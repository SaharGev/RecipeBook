package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R

class RecipeAdapter(
    private val recipes: List<RecipeItem>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    data class RecipeItem(
        val name: String,
        val description: String
    )

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRecipeName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvRecipeDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipes[position]
        holder.tvName.text = item.name
        holder.tvDescription.text = item.description
    }

    override fun getItemCount(): Int = recipes.size
}