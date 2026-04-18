package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.example.recipebook.db.BookEntity

class RecipeBooksAdapter(
    private val books: List<BookEntity>,
    private val onItemClick: (BookEntity) -> Unit,
    private val onDeleteClick: (BookEntity) -> Unit,
    private val countsMap: Map<Int, Int> = emptyMap(),
    private val bookImagesMap: Map<Int, List<String?>> = emptyMap()
) : RecyclerView.Adapter<RecipeBooksAdapter.BookViewHolder>() {

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvBookRecipesCount: TextView = itemView.findViewById(R.id.tvBookRecipesCount)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteBook)
        val imgBook1: ImageView = itemView.findViewById(R.id.imgBook1)
        val imgBook2: ImageView = itemView.findViewById(R.id.imgBook2)
        val imgBook3: ImageView = itemView.findViewById(R.id.imgBook3)
        val imgBook4: ImageView = itemView.findViewById(R.id.imgBook4)
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

        val images = bookImagesMap[book.id] ?: emptyList()
        val imageViews = listOf(holder.imgBook1, holder.imgBook2, holder.imgBook3, holder.imgBook4)

        imageViews.forEachIndexed { index, imageView ->
            Glide.with(holder.itemView.context)
                .load(images.getOrNull(index))
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView)
        }

        holder.itemView.setOnClickListener { onItemClick(book) }
        holder.btnDelete.setOnClickListener { onDeleteClick(book) }
    }

    override fun getItemCount(): Int = books.size
}