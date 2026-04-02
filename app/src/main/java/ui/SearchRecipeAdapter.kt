package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import android.widget.ImageView
import com.example.recipebook.model.Recipe

enum class SearchItemType {
    RECIPE,
    BOOK
}

data class SearchItem(
    val id: Int,
    val title: String,
    val type: SearchItemType,
    val imageUri: String? = null,
    val recipe: Recipe? = null
)

class SearchRecipeAdapter(
    private var items: List<SearchItem>,
    private val onItemClick: (SearchItem) -> Unit
) : RecyclerView.Adapter<SearchRecipeAdapter.ViewHolder>() {

    fun updateData(newItems: List<SearchItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView? = itemView.findViewById(R.id.imgRecipe)
        val recipeName: TextView? = itemView.findViewById(R.id.tvRecipeName)

        val bookImage: ImageView? = itemView.findViewById(R.id.imgBookPreview)
        val bookTitle: TextView? = itemView.findViewById(R.id.tvBookTitle)
        val bookRecipesCount: TextView? = itemView.findViewById(R.id.tvBookRecipesCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 0) {
            R.layout.item_search_recipe
        } else {
            R.layout.item_book
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        if (viewType == 1) {
            val params = view.layoutParams
            val displayMetrics = parent.context.resources.displayMetrics
            params.width = (displayMetrics.widthPixels / 2.2).toInt()
            view.layoutParams = params
        }

        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == SearchItemType.RECIPE) 0 else 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item.type == SearchItemType.RECIPE) {
            holder.recipeName?.text = item.title

            val imageUri = item.imageUri

            try {
                if (imageUri != null) {
                    holder.recipeImage?.setImageURI(android.net.Uri.parse(imageUri))
                } else {
                    holder.recipeImage?.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } catch (e: SecurityException) {
                holder.recipeImage?.setImageResource(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.bookTitle?.text = item.title
            holder.bookRecipesCount?.text = "${item.imageUri ?: "0"} recipes"
            holder.bookImage?.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }


    override fun getItemCount(): Int = items.size
}