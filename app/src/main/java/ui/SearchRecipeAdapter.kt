package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R

enum class SearchItemType {
    RECIPE,
    BOOK
}

data class SearchItem(
    val title: String,
    val type: SearchItemType
)

class SearchRecipeAdapter(
    private var items: List<SearchItem>
) : RecyclerView.Adapter<SearchRecipeAdapter.ViewHolder>() {

    fun updateData(newItems: List<SearchItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvRecipeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = items[position].title
    }

    override fun getItemCount(): Int = items.size
}