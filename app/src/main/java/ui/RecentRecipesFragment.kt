package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.model.Recipe
import com.example.recipebook.utils.RecentItemsHelper
import com.example.recipebook.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
class RecentRecipesFragment : Fragment(R.layout.fragment_recent_recipes) {

    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentRecipesFull = view.findViewById<RecyclerView>(R.id.rvRecentRecipesFull)
        rvRecentRecipesFull.layoutManager = GridLayoutManager(requireContext(), 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val recentIds = RecentItemsHelper.getRecentRecipeIds(requireContext(), uid)

        showLoading()
        recipeViewModel.getRecipes(uid) { recipes ->
            activity?.runOnUiThread {
                hideLoading()
                val recentRecipes = recentIds.mapNotNull { id ->
                    recipes.find { it.id == id }
                }
                val items = recentRecipes.map {
                    SearchItem(
                        id = it.id,
                        title = it.name,
                        type = SearchItemType.RECIPE,
                        imageUri = it.imageUri,
                        recipe = Recipe(
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
                    )
                }
                val adapter = SearchRecipeAdapter(items) { item ->
                    item.recipe?.let { recipe ->
                        val action = RecentRecipesFragmentDirections.actionRecentRecipesFragmentToRecipeDetailsFragment(
                            recipe = recipe
                        )
                        findNavController().navigate(action)
                    }
                }
                rvRecentRecipesFull.adapter = adapter
            }
        }
    }
}