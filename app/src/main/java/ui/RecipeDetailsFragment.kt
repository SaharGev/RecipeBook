package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.recipebook.R
import com.example.recipebook.model.Recipe
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class RecipeDetailsFragment : Fragment(R.layout.fragment_recipe_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val recipe = arguments?.getParcelable<com.example.recipebook.model.Recipe>("recipe")

        val tvTitle = view.findViewById<TextView>(R.id.tvRecipeTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvRecipeDescription)
        val tvIngredients = view.findViewById<TextView>(R.id.tvIngredients)
        val tvInstructions = view.findViewById<TextView>(R.id.tvInstructions)
        val imgRecipe = view.findViewById<android.widget.ImageView>(R.id.imgRecipe)
        val btnBack = view.findViewById<android.widget.Button>(R.id.btnBack)

        if (recipe != null) {
            tvTitle.text = "${recipe.name} (ID: ${recipe.id})"
            tvDescription.text = recipe.description
            tvIngredients.text = "Ingredients: ${recipe.ingredients}"
            tvInstructions.text = "Instructions: ${recipe.instructions}"

            recipe.imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imgRecipe)
            }

        } else {
            tvTitle.text = "No recipe received"
        }
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        (requireActivity() as androidx.appcompat.app.AppCompatActivity)
            .supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}