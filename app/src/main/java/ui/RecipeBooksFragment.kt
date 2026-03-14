package com.example.recipebook.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.BookViewModel

class RecipeBooksFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_recipe_books, container, false)

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvBooks)
        rvBooks.layoutManager = LinearLayoutManager(requireContext())

        val btnAddBook = view.findViewById<Button>(R.id.btnAddBook)

        btnAddBook.setOnClickListener {
            viewModel.addBook("My Recipe Book")
            loadBooks(rvBooks)
        }

        loadBooks(rvBooks)

        return view
    }

    private fun loadBooks(rvBooks: RecyclerView) {
        viewModel.getBooks { books ->
            rvBooks.post {
                rvBooks.adapter = RecipeBooksAdapter(
                    books,
                    onItemClick = { clickedBook ->
                        val bundle = Bundle()
                        bundle.putInt("bookId", clickedBook.id)
                        bundle.putString("bookTitle", clickedBook.title)

                        findNavController().navigate(
                            R.id.action_homeFragment_to_bookRecipesFragment,
                            bundle
                        )
                    },
                    onEditClick = { clickedBook ->
                        val editText = EditText(requireContext())
                        editText.setText(clickedBook.title)

                        AlertDialog.Builder(requireContext())
                            .setTitle("Edit Book")
                            .setView(editText)
                            .setPositiveButton("Save") { _, _ ->
                                val newTitle = editText.text.toString().trim()

                                if (newTitle.isNotEmpty()) {
                                    viewModel.updateBook(clickedBook.id, newTitle)
                                    Toast.makeText(
                                        requireContext(),
                                        "Book updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    loadBooks(rvBooks)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Book title cannot be empty",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    },
                    onDeleteClick = { clickedBook ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Book")
                            .setMessage("Are you sure you want to delete ${clickedBook.title}?")
                            .setPositiveButton("Yes") { _, _ ->
                                viewModel.deleteBook(clickedBook.id)
                                Toast.makeText(
                                    requireContext(),
                                    "Book deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadBooks(rvBooks)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                )
            }
        }
    }
}