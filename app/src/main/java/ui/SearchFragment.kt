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

class SearchFragment : Fragment(R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentSearch = view.findViewById<RecyclerView>(R.id.rvRecentSearch)
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)

        var allItems = listOf<SearchItem>()
        val adapter = SearchRecipeAdapter(allItems)

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

            val recipeItems = recipes.map { SearchItem(it.name, SearchItemType.RECIPE) }
            val bookItems = books.map { SearchItem(it.title, SearchItemType.BOOK) }

            allItems = recipeItems + bookItems

            adapter.updateData(allItems)
        }

        rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        rvRecentSearch.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentSearch.adapter = adapter


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                val filtered = allItems.filter {
                    it.title.lowercase().contains(query)
                }

                adapter.updateData(filtered)
            }
        })
    }
}