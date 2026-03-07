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
            findNavController().navigate(R.id.homeFragment)
        }
        tvBookTitle.text = bookTitle
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        btnAddRecipe.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("bookId", bookId)
            findNavController().navigate(R.id.addRecipeFragment, bundle)
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
        viewModel.getRecipesByBookId(bookId) { recipes ->
            rvRecipes.post {
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
                        imageUri = it.imageUri
                    )
                }

                rvRecipes.adapter = RecipeAdapter(
                    uiRecipes,
                    onItemClick = { clickedRecipe ->
                        val bundle = Bundle()
                        bundle.putParcelable("recipe", clickedRecipe)
                        findNavController().navigate(
                            R.id.recipeDetailsFragment,
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
                            imageUri = clickedRecipe.imageUri
                        )
                        viewModel.deleteRecipe(recipeToDelete)
                        loadRecipes(bookId, rvRecipes)
                    }
                )
            }
        }
    }
}