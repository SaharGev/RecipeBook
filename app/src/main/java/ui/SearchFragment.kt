package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText
import com.example.recipebook.db.DatabaseProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager

class SearchFragment : Fragment(R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentSearch = view.findViewById<RecyclerView>(R.id.rvRecentSearch)
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        val rvBooks = view.findViewById<RecyclerView>(R.id.rvBooks)

        var allItems = listOf<SearchItem>()
        var allBooks = listOf<SearchItem>()
        val adapter = SearchRecipeAdapter(allItems) { item ->
            item.recipe?.let { recipe ->
                findNavController().navigate(
                    R.id.action_searchFragment_to_recipeDetailsFragment,
                    bundleOf("recipe" to recipe)
                )
            }
        }

        val booksAdapter = SearchRecipeAdapter(emptyList<SearchItem>()) { item ->
            findNavController().navigate(
                R.id.action_searchFragment_to_bookRecipesFragment,
                bundleOf(
                    "bookId" to item.id,
                    "bookTitle" to item.title
                )
            )
        }

        val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert")
        val categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            val filtered = allItems.filter {
                it.title.lowercase().contains(selectedCategory.lowercase())
            }
            adapter.updateData(filtered)
        }

        val db = DatabaseProvider.getDatabase(requireContext())
        val recipeDao = db.recipeDao()
        val bookDao = db.bookDao()

        lifecycleScope.launch {
            val recipes = recipeDao.getAllRecipes()
            val books = bookDao.getAllBooks()

            val recipeItems = recipes.map {
                SearchItem(
                    id = it.id,
                    title = it.name,
                    type = SearchItemType.RECIPE,
                    imageUri = it.imageUri,
                    recipe = com.example.recipebook.model.Recipe(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        ingredients = it.ingredients,
                        instructions = it.instructions,
                        imageUri = it.imageUri,
                        cookTime = it.cookTime,
                        difficulty = it.difficulty,
                        isPublic = it.isPublic
                    )
                )
            }

            val bookItems = books.map { book ->
                val count = recipeDao.getRecipesByBookId(book.id).size

                SearchItem(
                    id = book.id,
                    title = book.title,
                    type = SearchItemType.BOOK,
                    imageUri = count.toString() // זמנית נשתמש בזה להעביר את הכמות
                )
            }

            allItems = recipeItems
            allBooks = bookItems

            adapter.updateData(allItems)
            booksAdapter.updateData(allBooks)
        }

        rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        rvRecentSearch.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentSearch.adapter = adapter

        rvBooks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvBooks.adapter = booksAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                val filteredRecipes = allItems.filter {
                    it.title.lowercase().contains(query)
                }

                val filteredBooks = allBooks.filter {
                    it.title.lowercase().contains(query)
                }

                adapter.updateData(filteredRecipes)
                booksAdapter.updateData(filteredBooks)
            }
        })
    }
}