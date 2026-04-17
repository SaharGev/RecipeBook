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

class SharedBooksFragment : Fragment(R.layout.fragment_shared_books) {

    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSharedBooksFull = view.findViewById<RecyclerView>(R.id.rvSharedBooksFull)
        rvSharedBooksFull.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        showLoading()
        bookViewModel.getSharedWithMeBooks(uid) { books ->
            activity?.runOnUiThread {
                hideLoading()
                val filtered = books.filter { it.ownerUid != uid }.sortedByDescending { it.id }
                val items = filtered.map {
                    SearchItem(
                        id = it.id,
                        title = it.title,
                        type = SearchItemType.BOOK,
                        imageUri = null
                    )
                }
                val adapter = SearchRecipeAdapter(items) { item ->
                    findNavController().navigate(
                        R.id.action_sharedBooksFragment_to_bookRecipesFragment,
                        bundleOf("bookId" to item.id, "bookTitle" to item.title)
                    )
                }
                rvSharedBooksFull.adapter = adapter
            }
        }
    }
}