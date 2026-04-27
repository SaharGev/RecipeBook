//ui/BookRecipesFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.repository.RecipeBookRepository
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.example.recipebook.utils.RecentItemsHelper
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class BookRecipesFragment : Fragment() {

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var tvEmptyState: TextView
    private val userViewModel: UserViewModel by viewModels()
    val bookViewModel: BookViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_book_recipes, container, false)

        val args = BookRecipesFragmentArgs.fromBundle(requireArguments())
        val bookId = args.bookId
        val bookTitle = args.bookTitle
        val tvDescription = view.findViewById<TextView>(R.id.tvBookDescription)
        val tvCount = view.findViewById<TextView>(R.id.tvRecipesCount)

        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        val tvSharedWith = view.findViewById<TextView>(R.id.tvSharedWith)

        bookViewModel.getBookById(bookId, uid) { book ->
            view?.post {
                tvDescription.text = book?.description ?: ""

                if (!book?.sharedWith.isNullOrEmpty()) {
                    val sharedUids = book.sharedWith
                        ?.split(",")
                        ?.map { it.split(":")[0] }
                        ?: emptyList()

                    userViewModel.getFriends(uid) { friends ->
                        activity?.runOnUiThread {
                            val sharedNames = friends
                                .filter { sharedUids.contains(it.uid) }
                                .map { it.username }

                            if (sharedNames.isNotEmpty()) {
                                tvSharedWith.visibility = View.VISIBLE

                                val text = "Shared with: ${sharedNames.joinToString(", ")}"
                                val spannable = android.text.SpannableString(text)

                                spannable.setSpan(
                                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                    0,
                                    "Shared with:".length,
                                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )

                                tvSharedWith.text = spannable
                            }
                        }
                    }
                } else {
                    tvSharedWith.visibility = View.VISIBLE
                }
            }
        }

        viewModel.getRecipesByBookId(bookId) { recipes ->
            view?.post {
                tvCount.text = "${recipes.size} recipes"
            }
        }

        val tvBookTitle = view.findViewById<TextView>(R.id.tvBookTitle)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val btnEdit = view.findViewById<ImageButton>(R.id.btnEditBook)

        btnEdit.setOnClickListener {
            val action =
                BookRecipesFragmentDirections
                    .actionBookRecipesFragmentToEditRecipeBookFragment(
                        bookId = bookId,
                        bookTitle = bookTitle,
                        bookDescription = tvDescription.text.toString()
                    )

            findNavController().navigate(action)
        }


        bookViewModel.getBookById(bookId, uid) { book ->
            view?.post {
                tvBookTitle.text = book?.title ?: ""
            }
        }

        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        //rvRecipes.setHasFixedSize(true)

        btnAddRecipe.setOnClickListener {
            val action = BookRecipesFragmentDirections.actionBookRecipesFragmentToAddRecipeFragment(
                bookId = bookId
            )
            findNavController().navigate(action)
        }

        loadRecipes(bookId, rvRecipes)

        return view
    }

    override fun onResume() {
        super.onResume()

        val bookId = BookRecipesFragmentArgs.fromBundle(requireArguments()).bookId
        view?.findViewById<RecyclerView>(R.id.rvRecipes)?.let {
            loadRecipes(bookId, it)
        }
    }

    private fun loadRecipes(bookId: Int, rvRecipes: RecyclerView) {
        showLoading()
        viewModel.getRecipesByBookId(bookId) { recipes ->
            rvRecipes.post {
                hideLoading()
                if (recipes.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    rvRecipes.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    rvRecipes.visibility = View.VISIBLE
                }
                val uiRecipes = recipes.map {
                    com.example.recipebook.model.Recipe(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        ingredients = it.ingredients,
                        instructions = it.instructions,
                        imageUri = it.imageUri,
                        cookTime = it.cookTime,
                        difficulty = it.difficulty,
                        isPublic = it.isPublic
                    )
                }

                rvRecipes.adapter = RecipeAdapter(
                    uiRecipes,
                    onItemClick = { clickedRecipe ->
                        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                        RecentItemsHelper.saveRecentRecipe(requireContext(), clickedRecipe.id, uid)
                        val action = BookRecipesFragmentDirections.actionBookRecipesFragmentToRecipeDetailsFragment(
                            recipe = clickedRecipe
                        )
                        findNavController().navigate(action)
                    },
                    onDeleteClick = { clickedRecipe ->
                        val recipeToDelete = com.example.recipebook.db.RecipeEntity(
                            id = clickedRecipe.id,
                            //bookId = bookId,
                            name = clickedRecipe.name,
                            description = clickedRecipe.description,
                            ingredients = clickedRecipe.ingredients,
                            instructions = clickedRecipe.instructions,
                            imageUri = clickedRecipe.imageUri,
                            cookTime = clickedRecipe.cookTime,
                            difficulty = clickedRecipe.difficulty,
                            isPublic = clickedRecipe.isPublic
                        )

                        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

                        viewLifecycleOwner.lifecycleScope.launch {
                            val repo = RecipeBookRepository(requireContext())

                            repo.removeRecipeFromBook(bookId, clickedRecipe.id)
                            repo.removeRecipeFromBookFirestore(uid, bookId, clickedRecipe.id)
                        }

                        rvRecipes.postDelayed({
                            loadRecipes(bookId, rvRecipes)
                        }, 100)
                    }
                )
            }
        }
    }
}