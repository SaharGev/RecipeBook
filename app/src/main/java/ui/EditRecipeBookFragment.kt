package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.recipebook.R
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.ui.RecipeAdapter
import com.example.recipebook.repository.RecipeBookRepository
import com.example.recipebook.viewmodel.UserViewModel

class EditRecipeBookFragment : Fragment(R.layout.fragment_edit_recipe_book) {

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val selectedFriendPermissions = mutableMapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = EditRecipeBookFragmentArgs.fromBundle(requireArguments())
        val bookId = args.bookId

        val etName = view.findViewById<EditText>(R.id.etBookName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)
        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        val llFriendsList = view.findViewById<LinearLayout>(R.id.llFriendsList)
        val headerShareWith = view.findViewById<LinearLayout>(R.id.headerShareWith)
        val tvShareWithArrow = view.findViewById<TextView>(R.id.tvShareWithArrow)

        headerShareWith.setOnClickListener {
            if (llFriendsList.visibility == View.GONE) {
                llFriendsList.visibility = View.VISIBLE
                tvShareWithArrow.text = "▲"
            } else {
                llFriendsList.visibility = View.GONE
                tvShareWithArrow.text = "▼"
            }
        }

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        bookViewModel.getBookById(bookId, uid) { book ->
            activity?.runOnUiThread {
                if (book != null) {
                    etName.setText(book.title)
                    etDescription.setText(book.description)
                }
            }

            val sharedUids = book?.sharedWith
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map { it.split(":")[0] }
                ?: emptyList()

            userViewModel.getFriends(uid) { friends ->
                requireActivity().runOnUiThread {
                    llFriendsList.removeAllViews()
                    selectedFriendPermissions.clear()

                    friends.forEach { friend ->
                        val checkBox = CheckBox(requireContext()).apply {
                            text = friend.username
                            isChecked = sharedUids.contains(friend.uid)
                        }

                        if (checkBox.isChecked) {
                            selectedFriendPermissions[friend.uid] = "view"
                        }

                        checkBox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                selectedFriendPermissions[friend.uid] = "view"
                            } else {
                                selectedFriendPermissions.remove(friend.uid)
                            }
                        }

                        llFriendsList.addView(checkBox)
                    }
                }
            }
        }

        loadRecipes(bookId, rvRecipes)

        btnUpdate.setOnClickListener {
            val newName = etName.text.toString()
            val newDescription = etDescription.text.toString()

            val sharedWith = selectedFriendPermissions.entries
                .joinToString(",") { "${it.key}:${it.value}" }

            bookViewModel.updateBook(bookId, newName, newDescription, sharedWith)

            Toast.makeText(requireContext(), "Book updated", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        btnAddRecipe.setOnClickListener {
            val action =
                EditRecipeBookFragmentDirections
                    .actionEditRecipeBookFragmentToAddRecipeBookFragment()
            findNavController().navigate(action)
        }
    }

    private fun loadRecipes(bookId: Int, rv: RecyclerView) {
        recipeViewModel.getRecipesByBookId(bookId) { recipes ->
            rv.post {
                val adapter = RecipeAdapter(
                    recipes.map {
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
                    },
                    onItemClick = {},
                    onDeleteClick = { recipe ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            val repo = RecipeBookRepository(requireContext())
                            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                            repo.removeRecipeFromBookFirestore(uid, bookId, recipe.id)
                            loadRecipes(bookId, rv)
                        }
                    }
                )
                rv.adapter = adapter
            }
        }
    }
}