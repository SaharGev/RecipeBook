// ui/MyRecipesFragment.kt
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
import com.example.recipebook.model.Recipe
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.example.recipebook.utils.RecentItemsHelper

class MyRecipesFragment : Fragment() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    private lateinit var rvRecipes: RecyclerView
    private lateinit var tvEmptyRecipes: TextView
    private lateinit var btnBack: Button
    private lateinit var btnAddRecipe: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_recipes, container, false)

        rvRecipes = view.findViewById(R.id.rvRecipes)
        tvEmptyRecipes = view.findViewById(R.id.tvEmptyRecipes)
        btnBack = view.findViewById(R.id.btnBack)
        btnAddRecipe = view.findViewById(R.id.btnAddRecipe)

        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        rvRecipes.isNestedScrollingEnabled = false

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_myRecipesFragment_to_addRecipeFragment)
        }

        loadMyRecipes()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadMyRecipes()
    }

    private fun loadMyRecipes() {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        showLoading()
        recipeViewModel.loadRecipes(uid)
        recipeViewModel.recipes.observe(viewLifecycleOwner) { myRecipes ->
            hideLoading()
            val myOwnRecipes = myRecipes.filter { it.ownerUid == uid }

            if (myOwnRecipes.isEmpty()) {
                rvRecipes.visibility = View.GONE
                tvEmptyRecipes.visibility = View.VISIBLE
            } else {
                rvRecipes.visibility = View.VISIBLE
                tvEmptyRecipes.visibility = View.GONE

                val recipes = myOwnRecipes.map { entity ->
                    Recipe(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        ingredients = entity.ingredients,
                        instructions = entity.instructions,
                        imageUri = entity.imageUri,
                        cookTime = entity.cookTime,
                        difficulty = entity.difficulty,
                        isPublic = entity.isPublic,
                        ownerUid = entity.ownerUid,
                        sharedWith = entity.sharedWith
                    )
                }

                rvRecipes.adapter = RecipeAdapter(
                    recipes = recipes,
                    onItemClick = { recipe -> navigateToRecipeDetails(recipe) },
                    onDeleteClick = { recipe ->

                        val entity = com.example.recipebook.db.RecipeEntity(
                            id = recipe.id,
                            name = recipe.name,
                            description = recipe.description,
                            ingredients = recipe.ingredients,
                            instructions = recipe.instructions,
                            imageUri = recipe.imageUri,
                            cookTime = recipe.cookTime,
                            difficulty = recipe.difficulty,
                            isPublic = recipe.isPublic,
                            ownerUid = recipe.ownerUid,
                            sharedWith = recipe.sharedWith
                        )

                        recipeViewModel.deleteRecipe(entity)
                    }
                )
            }
        }
    }

    private fun navigateToRecipeDetails(recipe: Recipe) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        RecentItemsHelper.saveRecentRecipe(requireContext(), recipe.id, uid)
        val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToRecipeDetailsFragment(
            recipe = recipe
        )
        findNavController().navigate(action)
    }
}