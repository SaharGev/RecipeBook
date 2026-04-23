package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.BookViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
class SharedBooksFragment : Fragment(R.layout.fragment_shared_books) {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSharedBooksFull = view.findViewById<RecyclerView>(R.id.rvSharedBooksFull)
        rvSharedBooksFull.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        showLoading()
        bookViewModel.getSharedWithMeBooks(uid) { books ->
            lifecycleScope.launch {
                hideLoading()
                val filtered = books.filter { it.ownerUid != uid }.sortedByDescending { it.id }
                val recipeDao = com.example.recipebook.db.DatabaseProvider.getDatabase(requireContext()).recipeDao()
                val recipeBookDao = com.example.recipebook.db.DatabaseProvider.getDatabase(requireContext()).recipeBookDao()
                val items = filtered.map { book ->
                    val recipes = recipeBookDao.getRecipesForBook(book.id)
                    SearchItem(
                        id = book.id,
                        title = book.title,
                        type = SearchItemType.BOOK,
                        imageUri = null,
                        bookImages = recipes.take(4).map { it.imageUri }
                    )
                }
                activity?.runOnUiThread {
                    val adapter = SearchRecipeAdapter(items) { item ->
                        val action = SharedBooksFragmentDirections.actionSharedBooksFragmentToBookRecipesFragment(
                            bookId = item.id,
                            bookTitle = item.title
                        )
                        findNavController().navigate(action)
                    }
                    rvSharedBooksFull.adapter = adapter
                }
            }
        }
    }
}