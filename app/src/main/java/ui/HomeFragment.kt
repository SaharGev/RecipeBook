package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R
import androidx.fragment.app.viewModels
import com.example.recipebook.viewmodel.RecipeViewModel

class HomeFragment : Fragment() {

    private val viewModel: RecipeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        val btnAddRecipe = view.findViewById<Button>(R.id.btnAddRecipe)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        btnAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addRecipeFragment)
        }

        viewModel.getRecipes { recipes ->

            rvRecipes.post {

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

                rvRecipes.adapter = RecipeAdapter(uiRecipes) { clickedRecipe ->

                    val bundle = Bundle()
                    bundle.putParcelable("recipe", clickedRecipe)

                    findNavController().navigate(
                        R.id.action_homeFragment_to_recipeDetailsFragment,
                        bundle
                    )
                }
            }
        }

        return view
    }
}