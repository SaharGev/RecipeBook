//ui/BookRecipesFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.example.recipebook.utils.RecentItemsHelper

class BookRecipesFragment : Fragment() {

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_book_recipes, container, false)

        val bookId = arguments?.getInt("bookId") ?: -1
        val bookTitle = arguments?.getString("bookTitle") ?: "Recipe Book"

        val tvBookTitle = view.findViewById<TextView>(R.id.tvBookTitle)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        tvBookTitle.text = bookTitle
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        //rvRecipes.setHasFixedSize(true)

        btnAddRecipe.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookId", bookId)
            findNavController().navigate(
                R.id.action_bookRecipesFragment_to_addRecipeFragment,
                bundle
            )
        }

        loadRecipes(bookId, rvRecipes)

        return view
    }

    override fun onResume() {
        super.onResume()

        val bookId = arguments?.getInt("bookId") ?: -1
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
                        val bundle = Bundle()
                        bundle.putParcelable("recipe", clickedRecipe)
                        findNavController().navigate(
                            R.id.action_bookRecipesFragment_to_recipeDetailsFragment,
                            bundle
                        )
                    },
                    onDeleteClick = { clickedRecipe ->
                        val recipeToDelete = com.example.recipebook.db.RecipeEntity(
                            id = clickedRecipe.id,
                            bookId = bookId,
                            name = clickedRecipe.name,
                            description = clickedRecipe.description,
                            ingredients = clickedRecipe.ingredients,
                            instructions = clickedRecipe.instructions,
                            imageUri = clickedRecipe.imageUri,
                            cookTime = clickedRecipe.cookTime,
                            difficulty = clickedRecipe.difficulty,
                            isPublic = clickedRecipe.isPublic
                        )

                        viewModel.deleteRecipe(recipeToDelete)

                        rvRecipes.postDelayed({
                            loadRecipes(bookId, rvRecipes)
                        }, 100)
                    }
                )
            }
        }
    }
}