package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.example.recipebook.api.Meal

class MealAdapter(
    private var meals: List<Meal>,
    private val onClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgRecipe: ImageView = view.findViewById(R.id.imgRecipe)
        val tvRecipeName: TextView = view.findViewById(R.id.tvRecipeName)
        val tvRecipeDescription: TextView = view.findViewById(R.id.tvRecipeDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.tvRecipeName.text = meal.strMeal
        holder.tvRecipeDescription.text = meal.strIngredient1 ?: ""
        holder.itemView.findViewById<android.widget.ImageButton>(R.id.btnDelete).visibility = View.GONE
        Glide.with(holder.itemView.context)
            .load(meal.strMealThumb)
            .into(holder.imgRecipe)
        holder.itemView.setOnClickListener { onClick(meal) }
    }

    override fun getItemCount() = meals.size

    fun updateData(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}