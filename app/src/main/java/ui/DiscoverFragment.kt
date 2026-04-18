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
import com.example.recipebook.api.Meal
import com.example.recipebook.viewmodel.MealViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class DiscoverFragment : Fragment(R.layout.fragment_discover) {

    private val mealViewModel: MealViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvDiscoverFull = view.findViewById<RecyclerView>(R.id.rvDiscoverFull)
        rvDiscoverFull.layoutManager = GridLayoutManager(requireContext(), 2)

        showLoading()
        mealViewModel.getRandomMeals(20) { meals ->
            activity?.runOnUiThread {
                hideLoading()
                val adapter = MealAdapter(meals) { meal ->
                    val action = DiscoverFragmentDirections.actionDiscoverFragmentToMealDetailsFragment(
                        meal = meal.idMeal
                    )
                    findNavController().navigate(action)
                }
                rvDiscoverFull.adapter = adapter
            }
        }
    }
}