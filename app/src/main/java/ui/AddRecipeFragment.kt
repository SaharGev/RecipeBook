package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import com.example.recipebook.viewmodel.RecipeViewModel

class AddRecipeFragment : Fragment(R.layout.fragment_add_recipe) {

    private val viewModel: RecipeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etIngredients = view.findViewById<EditText>(R.id.etIngredients)
        val etInstructions = view.findViewById<EditText>(R.id.etInstructions)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {

            viewModel.addRecipe(
                bookId = 1,
                name = etName.text.toString(),
                description = etDescription.text.toString(),
                ingredients = etIngredients.text.toString(),
                instructions = etInstructions.text.toString()
            )

            findNavController().popBackStack()
        }
    }
}