package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.viewmodel.RecipeViewModel

class RecipeBooksFragment : Fragment() {

    private val viewModel: BookViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recipe_books, container, false)

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvBooks)
        rvBooks.layoutManager = GridLayoutManager(requireContext(), 2)

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
            val countsMap = mutableMapOf<Int, Int>()

            if (books.isEmpty()) {
                rvBooks.post {
                    rvBooks.adapter = RecipeBooksAdapter(
                        books = books,
                        onItemClick = { clickedBook ->
                            val bundle = Bundle()
                            bundle.putInt("bookId", clickedBook.id)
                            bundle.putString("bookTitle", clickedBook.title)

                            findNavController().navigate(
                                R.id.action_homeFragment_to_bookRecipesFragment,
                                bundle
                            )
                        },
                        countsMap = countsMap
                    )
                }
                return@getBooks
            }

            var remaining = books.size

            books.forEach { book ->
                recipeViewModel.getRecipesCountByBookId(book.id) { count ->
                    countsMap[book.id] = count
                    remaining--

                    if (remaining == 0) {
                        rvBooks.post {
                            rvBooks.adapter = RecipeBooksAdapter(
                                books = books,
                                onItemClick = { clickedBook ->
                                    val bundle = Bundle()
                                    bundle.putInt("bookId", clickedBook.id)
                                    bundle.putString("bookTitle", clickedBook.title)

                                    findNavController().navigate(
                                        R.id.action_homeFragment_to_bookRecipesFragment,
                                        bundle
                                    )
                                },
                                countsMap = countsMap
                            )
                        }
                    }
                }
            }
        }
    }
}