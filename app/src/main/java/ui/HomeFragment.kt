package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        val dummyRecipes = listOf(
            RecipeAdapter.RecipeItem("Pasta", "Quick and tasty"),
            RecipeAdapter.RecipeItem("Salad", "Fresh and healthy"),
            RecipeAdapter.RecipeItem("Soup", "Warm and comforting")
        )

        rvRecipes.adapter = RecipeAdapter(dummyRecipes)

        return view
    }
}