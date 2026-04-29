package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.example.recipebook.viewmodel.MealViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading

class MealDetailsFragment : Fragment(R.layout.fragment_meal_details) {

    private val mealViewModel: MealViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = MealDetailsFragmentArgs.fromBundle(requireArguments())
        val mealId = args.meal

        val imgMeal = view.findViewById<ImageView>(R.id.imgMeal)
        val tvMealName = view.findViewById<TextView>(R.id.tvMealName)
        val tvMealIngredientsList = view.findViewById<TextView>(R.id.tvMealIngredientsList)
        val tvMealInstructionsList = view.findViewById<TextView>(R.id.tvMealInstructionsList)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        showLoading()
        mealViewModel.getMealById(mealId) { meal ->
            activity?.runOnUiThread {
                hideLoading()
                if (meal == null) return@runOnUiThread

                tvMealName.text = meal.strMeal
                tvMealInstructionsList.text = meal.strInstructions ?: ""

                val ingredients = listOfNotNull(
                    meal.strIngredient1,
                    meal.strIngredient2,
                    meal.strIngredient3,
                    meal.strIngredient4,
                    meal.strIngredient5
                ).filter { it.isNotBlank() }.joinToString("\n• ", "• ")
                tvMealIngredientsList.text = ingredients

                Glide.with(requireContext())
                    .load(meal.strMealThumb)
                    .into(imgMeal)
            }
        }
    }
}