package com.example.recipebook.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.model.Recipe
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentSearch = view.findViewById<RecyclerView>(R.id.rvRecentSearch)
        val rvResults = view.findViewById<RecyclerView>(R.id.rvResults)
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        val rvBooks = view.findViewById<RecyclerView>(R.id.rvBooks)

        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)

        val tvRecentRecipesTitle  = view.findViewById<TextView>(R.id.tvRecentSearch)
        val tvResultsTitle = view.findViewById<TextView>(R.id.tvResultsTitle)
        val tvRecentBooksTitle = view.findViewById<TextView>(R.id.tvRecentBooksTitle)
        val tvBooksResultsTitle = view.findViewById<TextView>(R.id.tvBooksResultsTitle)

        val tvFavoriteRecipesTitle = view.findViewById<TextView>(R.id.tvFavoriteRecipesTitle)
        val tvFavoriteRecipesEmpty = view.findViewById<TextView>(R.id.tvFavoriteRecipesEmpty)
        val tvFavoriteBooksTitle = view.findViewById<TextView>(R.id.tvFavoriteBooksTitle)
        val tvFavoriteBooksEmpty = view.findViewById<TextView>(R.id.tvFavoriteBooksEmpty)
        val tvSharedRecipesTitle = view.findViewById<TextView>(R.id.tvSharedRecipesTitle)
        val tvSharedRecipesEmpty = view.findViewById<TextView>(R.id.tvSharedRecipesEmpty)
        val tvSharedBooksTitle = view.findViewById<TextView>(R.id.tvSharedBooksTitle)
        val tvSharedBooksEmpty = view.findViewById<TextView>(R.id.tvSharedBooksEmpty)

        var allItems = listOf<SearchItem>()
        var allBooks = listOf<SearchItem>()

        val recentAdapter = SearchRecipeAdapter(emptyList()) { item ->
            item.recipe?.let { recipe ->
                saveRecentRecipe(item.id)
                findNavController().navigate(
                    R.id.action_searchFragment_to_recipeDetailsFragment,
                    bundleOf("recipe" to recipe)
                )
            }
        }

        val resultsAdapter = SearchRecipeAdapter(emptyList()) { item ->
            item.recipe?.let { recipe ->
                saveRecentRecipe(item.id)
                findNavController().navigate(
                    R.id.action_searchFragment_to_recipeDetailsFragment,
                    bundleOf("recipe" to recipe)
                )
            }
        }

        val booksAdapter = SearchRecipeAdapter(emptyList()) { item ->
            saveRecentBook(item.id)
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

            tvResultsTitle.visibility = View.VISIBLE
            rvResults.visibility = View.VISIBLE
            tvRecentRecipesTitle.visibility = View.GONE
            rvRecentSearch.visibility = View.GONE

            resultsAdapter.updateData(filtered)
        }

        rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = categoryAdapter

        rvRecentSearch.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentSearch.adapter = recentAdapter

        rvResults.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvResults.adapter = resultsAdapter

        rvBooks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvBooks.adapter = booksAdapter

        tvResultsTitle.visibility = View.GONE
        rvResults.visibility = View.GONE
        tvRecentRecipesTitle.visibility = View.VISIBLE
        rvRecentSearch.visibility = View.VISIBLE

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
                    recipe = Recipe(
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
                    imageUri = count.toString()
                )
            }

            allItems = recipeItems
            allBooks = bookItems

            val recentRecipeIds = getRecentRecipeIds()
            val recentRecipeItems = recentRecipeIds.mapNotNull { id ->
                recipeItems.find { it.id == id && it.type == SearchItemType.RECIPE }
            }

            recentAdapter.updateData(recentRecipeItems)
            val recentBookIds = getRecentBookIds()
            val recentBookItems = recentBookIds.mapNotNull { id ->
                bookItems.find { it.id == id && it.type == SearchItemType.BOOK }
            }

            booksAdapter.updateData(recentBookItems)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                val recentRecipeIds = getRecentRecipeIds()
                val recentRecipeItems = recentRecipeIds.mapNotNull { id ->
                    allItems.find { it.id == id && it.type == SearchItemType.RECIPE }
                }

                if (query.isBlank()) {
                    tvResultsTitle.visibility = View.GONE
                    rvResults.visibility = View.GONE
                    tvRecentRecipesTitle.visibility = View.VISIBLE
                    rvRecentSearch.visibility = View.VISIBLE

                    tvRecentBooksTitle.visibility = View.VISIBLE
                    tvBooksResultsTitle.visibility = View.GONE
                    rvBooks.visibility = View.VISIBLE

                    recentAdapter.updateData(recentRecipeItems)
                    val recentBookIds = getRecentBookIds()
                    val recentBookItems = recentBookIds.mapNotNull { id ->
                        allBooks.find { it.id == id && it.type == SearchItemType.BOOK }
                    }

                    booksAdapter.updateData(recentBookItems)

                    tvFavoriteRecipesTitle.visibility = View.VISIBLE
                    tvFavoriteRecipesEmpty.visibility = View.VISIBLE
                    tvFavoriteBooksTitle.visibility = View.VISIBLE
                    tvFavoriteBooksEmpty.visibility = View.VISIBLE
                    tvSharedRecipesTitle.visibility = View.VISIBLE
                    tvSharedRecipesEmpty.visibility = View.VISIBLE
                    tvSharedBooksTitle.visibility = View.VISIBLE
                    tvSharedBooksEmpty.visibility = View.VISIBLE

                } else {
                    val filteredRecipes = allItems.filter {
                        it.title.lowercase().contains(query)
                    }

                    val filteredBooks = allBooks.filter {
                        it.title.lowercase().contains(query)
                    }

                    val recentIds = getRecentRecipeIds()

                    // TODO: add priority for favorites (recent + favorite -> recent -> rest)

                    val sortedRecipes = filteredRecipes.sortedWith(compareByDescending<SearchItem> {
                        recentIds.contains(it.id)
                    })

                    // TODO: add priority for favorites (recent + favorite -> recent -> rest)

                    val sortedBooks = filteredBooks.sortedWith(compareByDescending<SearchItem> {
                        getRecentBookIds().contains(it.id)
                    })

                    android.widget.Toast.makeText(requireContext(), "recipes=${filteredRecipes.size}, books=${filteredBooks.size}", android.widget.Toast.LENGTH_SHORT).show()

                    tvResultsTitle.visibility = View.VISIBLE
                    rvResults.visibility = View.VISIBLE
                    tvRecentRecipesTitle.visibility = View.GONE
                    rvRecentSearch.visibility = View.GONE

                    tvRecentBooksTitle.visibility = View.GONE
                    tvBooksResultsTitle.visibility = View.VISIBLE
                    rvBooks.visibility = View.VISIBLE

                    resultsAdapter.updateData(sortedRecipes)
                    booksAdapter.updateData(sortedBooks)

                    tvFavoriteRecipesTitle.visibility = View.GONE
                    tvFavoriteRecipesEmpty.visibility = View.GONE
                    tvFavoriteBooksTitle.visibility = View.GONE
                    tvFavoriteBooksEmpty.visibility = View.GONE
                    tvSharedRecipesTitle.visibility = View.GONE
                    tvSharedRecipesEmpty.visibility = View.GONE
                    tvSharedBooksTitle.visibility = View.GONE
                    tvSharedBooksEmpty.visibility = View.GONE

                }
            }
        })
    }

    private fun saveRecentRecipe(recipeId: Int) {
        val prefs = requireContext().getSharedPreferences("recent_recipes", Context.MODE_PRIVATE)
        val currentIds = prefs.getString("ids", "") ?: ""

        val idsList = currentIds
            .split(",")
            .filter { it.isNotBlank() && it != recipeId.toString() }
            .toMutableList()

        idsList.add(0, recipeId.toString())

        val limitedList = idsList.take(10)

        prefs.edit()
            .putString("ids", limitedList.joinToString(","))
            .apply()
    }

    private fun getRecentRecipeIds(): List<Int> {
        val prefs = requireContext().getSharedPreferences("recent_recipes", Context.MODE_PRIVATE)
        val idsString = prefs.getString("ids", "") ?: ""

        return idsString
            .split(",")
            .mapNotNull { it.toIntOrNull() }
    }

    private fun saveRecentBook(bookId: Int) {
        val prefs = requireContext().getSharedPreferences("recent_books", Context.MODE_PRIVATE)
        val currentIds = prefs.getString("ids", "") ?: ""

        val idsList = currentIds
            .split(",")
            .filter { it.isNotBlank() && it != bookId.toString() }
            .toMutableList()

        idsList.add(0, bookId.toString())

        val limitedList = idsList.take(10)

        prefs.edit()
            .putString("ids", limitedList.joinToString(","))
            .apply()
    }

    private fun getRecentBookIds(): List<Int> {
        val prefs = requireContext().getSharedPreferences("recent_books", Context.MODE_PRIVATE)
        val idsString = prefs.getString("ids", "") ?: ""

        return idsString
            .split(",")
            .mapNotNull { it.toIntOrNull() }
    }

}