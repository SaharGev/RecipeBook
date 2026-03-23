//ui/RecipeBooksAdapter
package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.BookEntity

class RecipeBooksAdapter(
    private val books: List<BookEntity>,
    private val onItemClick: (BookEntity) -> Unit,
    private val countsMap: Map<Int, Int> = emptyMap()
) : RecyclerView.Adapter<RecipeBooksAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvBookRecipesCount: TextView = itemView.findViewById(R.id.tvBookRecipesCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)

        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.tvBookTitle.text = book.title

        val recipesCount = countsMap[book.id] ?: 0
        holder.tvBookRecipesCount.text = "$recipesCount recipes"

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }
    }

    override fun getItemCount(): Int = books.size
}