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
import com.example.recipebook.utils.RecentItemsHelper
import com.example.recipebook.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth

class RecentBooksFragment : Fragment(R.layout.fragment_recent_books) {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentBooksFull = view.findViewById<RecyclerView>(R.id.rvRecentBooksFull)
        rvRecentBooksFull.layoutManager = GridLayoutManager(requireContext(), 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val recentIds = RecentItemsHelper.getRecentBookIds(requireContext(), uid)

        bookViewModel.getBooks(uid) { books ->
            activity?.runOnUiThread {
                val recentBooks = recentIds.mapNotNull { id ->
                    books.find { it.id == id }
                }
                val items = recentBooks.map {
                    SearchItem(
                        id = it.id,
                        title = it.title,
                        type = SearchItemType.BOOK,
                        imageUri = it.imageUri
                    )
                }
                val adapter = SearchRecipeAdapter(items) { item ->
                    findNavController().navigate(
                        R.id.action_recentBooksFragment_to_bookRecipesFragment,
                        bundleOf("bookId" to item.id, "bookTitle" to item.title)
                    )
                }
                rvRecentBooksFull.adapter = adapter
            }
        }
    }
}