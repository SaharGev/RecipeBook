//ui/RecipeBooksFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.example.recipebook.utils.RecentItemsHelper
import com.google.firebase.auth.FirebaseAuth

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
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvEmptyBooks = view.findViewById<TextView>(R.id.tvEmptyBooks)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnAddBook.setOnClickListener {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            viewModel.addBook(
                uid = uid,
                title = "My Recipe Book",
                onDone = {
                    requireActivity().runOnUiThread {
                        loadBooks(rvBooks, tvEmptyBooks)
                    }
                }
            )
        }

        loadBooks(rvBooks, tvEmptyBooks)

        return view
    }

    private fun loadBooks(rvBooks: RecyclerView, tvEmptyBooks: TextView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        showLoading()
        viewModel.loadBooks(uid)
        viewModel.books.observe(viewLifecycleOwner) { books ->
            if (books.isEmpty()) {
                hideLoading()
                rvBooks.visibility = View.GONE
                tvEmptyBooks.visibility = View.VISIBLE
                return@observe
            }

            val countsMap = mutableMapOf<Int, Int>()
            val bookImagesMap = mutableMapOf<Int, List<String?>>()
            var remaining = books.size

            books.forEach { book ->
                recipeViewModel.getRecipesByBookId(book.id) { recipes ->
                    countsMap[book.id] = recipes.size
                    bookImagesMap[book.id] = recipes.take(4).map { it.imageUri }
                    remaining--

                    if (remaining == 0) {
                        rvBooks.post {
                            hideLoading()
                            rvBooks.visibility = View.VISIBLE
                            tvEmptyBooks.visibility = View.GONE
                            rvBooks.adapter = RecipeBooksAdapter(
                                books = books,
                                onItemClick = { clickedBook ->
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                                    RecentItemsHelper.saveRecentBook(requireContext(), clickedBook.id, uid)
                                    val action = RecipeBooksFragmentDirections.actionHomeFragmentToBookRecipesFragment(
                                        bookId = clickedBook.id,
                                        bookTitle = clickedBook.title
                                    )
                                    findNavController().navigate(action)
                                },
                                onDeleteClick = { book ->
                                    viewModel.deleteBookAndDetachRecipes(book.id, recipeViewModel)
                                },
                                countsMap = countsMap,
                                bookImagesMap = bookImagesMap
                            )
                        }
                    }
                }
            }
        }
    }
}