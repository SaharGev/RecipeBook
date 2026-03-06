package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.recipebook.R
import com.example.recipebook.model.Recipe

class RecipeDetailsFragment : Fragment(R.layout.fragment_recipe_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipe = arguments?.getParcelable<com.example.recipebook.model.Recipe>("recipe")

        val tvTitle = view.findViewById<TextView>(R.id.tvRecipeTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvRecipeDescription)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredients)
        val tvInstructions = view.findViewById<TextView>(R.id.tvInstructions)

        if (recipe != null) {
            tvTitle.text = "${recipe.name} (ID: ${recipe.id})"
            tvDescription.text = recipe.description
            tvIngredients.text = "Ingredients: ${recipe.ingredients}"
            tvInstructions.text = "Instructions: ${recipe.instructions}"
        } else {
            tvTitle.text = "No recipe received"
        }
    }
}