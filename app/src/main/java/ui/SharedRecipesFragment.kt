package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.model.Recipe
import com.example.recipebook.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import androidx.recyclerview.widget.GridLayoutManager

class SharedRecipesFragment : Fragment(R.layout.fragment_shared_recipes) {

    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSharedRecipesFull = view.findViewById<RecyclerView>(R.id.rvSharedRecipesFull)

        rvSharedRecipesFull.layoutManager = GridLayoutManager(requireContext(), 2)
        rvSharedRecipesFull.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: androidx.recyclerview.widget.RecyclerView,
                state: androidx.recyclerview.widget.RecyclerView.State
            ) {
                outRect.bottom = 48
                outRect.top = 16
                outRect.left = 8
                outRect.right = 8
            }
        })

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        showLoading()
        recipeViewModel.getSharedWithMeRecipes(uid) { recipes ->
            activity?.runOnUiThread {
                hideLoading()
                val items = recipes.sortedByDescending { it.id }.map {
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
                        val action = SharedRecipesFragmentDirections.actionSharedRecipesFragmentToRecipeDetailsFragment(
                            recipe = recipe
                        )
                        findNavController().navigate(action)
                    }
                }
                rvSharedRecipesFull.adapter = adapter
            }
        }
    }
}