//ui/HomeFragment
package com.example.recipebook.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.viewmodel.RecipeViewModel

class HomeFragment : Fragment() {

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var tvEmptyState: android.widget.TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addRecipeFragment)
        }

        loadRecipes(rvRecipes)

        return view
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<RecyclerView>(R.id.rvRecipes)?.let {
            loadRecipes(it)
        }
    }

    private fun loadRecipes(rvRecipes: RecyclerView) {
        viewModel.getRecipes { recipes ->

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
                        imageUri = it.imageUri,
                        cookTime = it.cookTime,
                        isPublic = it.isPublic,
                        difficulty = it.difficulty
                    )
                }

                rvRecipes.adapter = RecipeAdapter(
                    uiRecipes,
                    onItemClick = { clickedRecipe ->
                        val bundle = Bundle()
                        bundle.putParcelable("recipe", clickedRecipe)

                        findNavController().navigate(
                            R.id.action_homeFragment_to_recipeDetailsFragment,
                            bundle
                        )
                    },
                    onDeleteClick  = { clickedRecipe ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Recipe")
                            .setMessage("Are you sure you want to delete ${clickedRecipe.name}?")
                            .setPositiveButton("Yes") { _, _ ->
                                val recipeToDelete = RecipeEntity(
                                    id = clickedRecipe.id,
                                    bookId = 1,
                                    name = clickedRecipe.name,
                                    description = clickedRecipe.description,
                                    ingredients = clickedRecipe.ingredients,
                                    instructions = clickedRecipe.instructions,
                                    imageUri = clickedRecipe.imageUri,
                                    cookTime = clickedRecipe.cookTime,
                                    isPublic = clickedRecipe.isPublic,
                                    difficulty = clickedRecipe.difficulty
                                )

                                viewModel.deleteRecipe(recipeToDelete)

                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "Recipe deleted successfully",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()

                                loadRecipes(rvRecipes)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                )
            }
        }
    }
}