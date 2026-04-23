package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.utils.RecentItemsHelper
import com.example.recipebook.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RecentBooksFragment : Fragment(R.layout.fragment_recent_books) {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentBooksFull = view.findViewById<RecyclerView>(R.id.rvRecentBooksFull)
        rvRecentBooksFull.layoutManager = GridLayoutManager(requireContext(), 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val recentIds = RecentItemsHelper.getRecentBookIds(requireContext(), uid)
        val recipeDao = DatabaseProvider.getDatabase(requireContext()).recipeDao()
        val recipeBookDao = DatabaseProvider.getDatabase(requireContext()).recipeBookDao()

        bookViewModel.getBooks(uid) { books ->
            lifecycleScope.launch {
                val recentBooks = recentIds.mapNotNull { id ->
                    books.find { it.id == id }
                }
                val items = recentBooks.map { book ->
                    val recipes = recipeBookDao.getRecipesForBook(book.id)
                    SearchItem(
                        id = book.id,
                        title = book.title,
                        type = SearchItemType.BOOK,
                        imageUri = book.imageUri,
                        bookImages = recipes.take(4).map { it.imageUri }
                    )
                }
                activity?.runOnUiThread {
                    val adapter = SearchRecipeAdapter(items) { item ->
                        val action = RecentBooksFragmentDirections.actionRecentBooksFragmentToBookRecipesFragment(
                            bookId = item.id,
                            bookTitle = item.title
                        )
                        findNavController().navigate(action)
                    }
                    rvRecentBooksFull.adapter = adapter
                }
            }
        }
    }
}