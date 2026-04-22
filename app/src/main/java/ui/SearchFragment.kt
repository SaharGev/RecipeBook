package com.example.recipebook.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.model.Recipe
import com.example.recipebook.utils.RecentItemsHelper
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.viewmodel.RecipeViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.recipebook.viewmodel.MealViewModel

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var discoverAdapter: MealAdapter
    private val mealViewModel: MealViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvRecentSearch = view.findViewById<RecyclerView>(R.id.rvRecentSearch)
        val rvResults = view.findViewById<RecyclerView>(R.id.rvResults)
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        val rvBooks = view.findViewById<RecyclerView>(R.id.rvBooks)

        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)

        val tvResultsTitle = view.findViewById<TextView>(R.id.tvResultsTitle)
        val tvBooksResultsTitle = view.findViewById<TextView>(R.id.tvBooksResultsTitle)

        val tvFavoriteRecipesEmpty = view.findViewById<TextView>(R.id.tvFavoriteRecipesEmpty)
        val tvFavoriteBooksEmpty = view.findViewById<TextView>(R.id.tvFavoriteBooksEmpty)
        val tvSharedRecipesEmpty = view.findViewById<TextView>(R.id.tvSharedRecipesEmpty)
        val tvSharedBooksEmpty = view.findViewById<TextView>(R.id.tvSharedBooksEmpty)

        val tvSeeAllRecentRecipes = view.findViewById<TextView>(R.id.tvSeeAllRecentRecipes)
        val tvSeeAllRecentBooks = view.findViewById<TextView>(R.id.tvSeeAllRecentBooks)
        val tvSeeAllSharedRecipes = view.findViewById<TextView>(R.id.tvSeeAllSharedRecipes)
        val tvSeeAllSharedBooks = view.findViewById<TextView>(R.id.tvSeeAllSharedBooks)

        val layoutRecentRecipesHeader = view.findViewById<View>(R.id.layoutRecentRecipesHeader)
        val layoutRecentBooksHeader = view.findViewById<View>(R.id.layoutRecentBooksHeader)

        val layoutFavoriteRecipesHeader = view.findViewById<View>(R.id.layoutFavoriteRecipesHeader)
        val layoutFavoriteBooksHeader = view.findViewById<View>(R.id.layoutFavoriteBooksHeader)
        val layoutSharedRecipesHeader = view.findViewById<View>(R.id.layoutSharedRecipesHeader)
        val layoutSharedBooksHeader = view.findViewById<View>(R.id.layoutSharedBooksHeader)

        var allItems = listOf<SearchItem>()
        var allBooks = listOf<SearchItem>()

        val rvDiscover = view.findViewById<RecyclerView>(R.id.rvDiscover)
        val layoutDiscoverHeader = view.findViewById<View>(R.id.layoutDiscoverHeader)
        val tvSeeAllDiscover = view.findViewById<TextView>(R.id.tvSeeAllDiscover)

        rvDiscover.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        discoverAdapter = MealAdapter(emptyList()) { meal ->
            val action = SearchFragmentDirections.actionSearchFragmentToMealDetailsFragment(
                meal = meal.idMeal
            )
            findNavController().navigate(action)
        }

        rvDiscover.adapter = discoverAdapter
        mealViewModel.getRandomMeals { meals ->
            activity?.runOnUiThread {
                discoverAdapter.updateData(meals)
            }
        }

        tvSeeAllDiscover.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_discoverFragment)
        }

        val recentAdapter = SearchRecipeAdapter(emptyList()) { item ->
            item.recipe?.let { recipe ->
                saveRecentRecipe(item.id)
                val action = SearchFragmentDirections.actionSearchFragmentToRecipeDetailsFragment(
                    recipe = recipe
                )
                findNavController().navigate(action)
            }
        }

        val resultsAdapter = SearchRecipeAdapter(emptyList()) { item ->
            item.recipe?.let { recipe ->
                saveRecentRecipe(item.id)
                val action = SearchFragmentDirections.actionSearchFragmentToRecipeDetailsFragment(
                    recipe = recipe
                )
                findNavController().navigate(action)
            }
        }

        val booksAdapter = SearchRecipeAdapter(emptyList()) { item ->
            saveRecentBook(item.id)
            val action = SearchFragmentDirections.actionSearchFragmentToBookRecipesFragment(
                bookId = item.id,
                bookTitle = item.title
            )
            findNavController().navigate(action)
        }

        tvSeeAllRecentRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_myRecipesFragment)
        }

        tvSeeAllRecentBooks.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
        }

        tvSeeAllSharedRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_sharedRecipesFragment)
        }

        tvSeeAllSharedBooks.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_sharedBooksFragment)
        }

        tvSeeAllRecentRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_recentRecipesFragment)
        }

        tvSeeAllRecentBooks.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_recentBooksFragment)
        }

        val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert")
        val categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            val filtered = allItems.filter {
                it.title.lowercase().contains(selectedCategory.lowercase())
            }

            tvResultsTitle.visibility = View.VISIBLE
            rvResults.visibility = View.VISIBLE

            layoutRecentRecipesHeader.visibility = View.GONE
            rvRecentSearch.visibility = View.GONE

            layoutRecentBooksHeader.visibility = View.GONE
            tvBooksResultsTitle.visibility = View.GONE
            rvBooks.visibility = View.GONE

            layoutFavoriteRecipesHeader.visibility = View.GONE
            tvFavoriteRecipesEmpty.visibility = View.GONE
            layoutFavoriteBooksHeader.visibility = View.GONE
            tvFavoriteBooksEmpty.visibility = View.GONE
            layoutSharedRecipesHeader.visibility = View.GONE
            tvSharedRecipesEmpty.visibility = View.GONE
            layoutSharedBooksHeader.visibility = View.GONE
            tvSharedBooksEmpty.visibility = View.GONE

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
                        isPublic = it.isPublic,
                        ownerUid = it.ownerUid
                    )
                )
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val myBooks = books.filter { it.ownerUid == uid }
            val bookItems = myBooks.map { book ->
                val recipes = recipeDao.getRecipesByBookId(book.id)
                val count = recipes.size
                SearchItem(
                    id = book.id,
                    title = book.title,
                    type = SearchItemType.BOOK,
                    imageUri = count.toString(),
                    bookImages = recipes.take(4).map { it.imageUri }
                )
            }

            allItems = recipeItems
            allBooks = bookItems

            showDefaultSections(
                allItems,
                allBooks,
                recentAdapter,
                booksAdapter,
                layoutRecentRecipesHeader,
                rvRecentSearch,
                tvResultsTitle,
                rvResults,
                layoutRecentBooksHeader,
                tvBooksResultsTitle,
                rvBooks,
                layoutFavoriteRecipesHeader,
                tvFavoriteRecipesEmpty,
                layoutFavoriteBooksHeader,
                tvFavoriteBooksEmpty,
                layoutSharedRecipesHeader,
                tvSharedRecipesEmpty,
                layoutSharedBooksHeader,
                tvSharedBooksEmpty,
                layoutDiscoverHeader,
                rvDiscover
            )
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                if (query.isBlank()) {
                    showDefaultSections(
                        allItems,
                        allBooks,
                        recentAdapter,
                        booksAdapter,
                        layoutRecentRecipesHeader,
                        rvRecentSearch,
                        tvResultsTitle,
                        rvResults,
                        layoutRecentBooksHeader,
                        tvBooksResultsTitle,
                        rvBooks,
                        layoutFavoriteRecipesHeader,
                        tvFavoriteRecipesEmpty,
                        layoutFavoriteBooksHeader,
                        tvFavoriteBooksEmpty,
                        layoutSharedRecipesHeader,
                        tvSharedRecipesEmpty,
                        layoutSharedBooksHeader,
                        tvSharedBooksEmpty,
                        layoutDiscoverHeader,
                        rvDiscover
                    )
                } else {
                    val filteredRecipes = allItems.filter {
                        it.title.lowercase().contains(query)
                    }

                    val filteredBooks = allBooks.filter {
                        it.title.lowercase().contains(query)
                    }

                    val recentRecipeIds = getRecentRecipeIds()
                    val recentBookIds = getRecentBookIds()

                    val sortedRecipes = filteredRecipes.sortedWith(
                        compareByDescending<SearchItem> { recentRecipeIds.contains(it.id) }
                    )

                    val sortedBooks = filteredBooks.sortedWith(
                        compareByDescending<SearchItem> { recentBookIds.contains(it.id) }
                    )

                    tvResultsTitle.visibility =
                        if (sortedRecipes.isNotEmpty()) View.VISIBLE else View.GONE
                    rvResults.visibility =
                        if (sortedRecipes.isNotEmpty()) View.VISIBLE else View.GONE

                    tvBooksResultsTitle.visibility =
                        if (sortedBooks.isNotEmpty()) View.VISIBLE else View.GONE
                    rvBooks.visibility =
                        if (sortedBooks.isNotEmpty()) View.VISIBLE else View.GONE

                    layoutRecentRecipesHeader.visibility = View.GONE
                    rvRecentSearch.visibility = View.GONE
                    layoutRecentBooksHeader.visibility = View.GONE

                    layoutFavoriteRecipesHeader.visibility = View.GONE
                    tvFavoriteRecipesEmpty.visibility = View.GONE
                    layoutFavoriteBooksHeader.visibility = View.GONE
                    tvFavoriteBooksEmpty.visibility = View.GONE
                    layoutSharedRecipesHeader.visibility = View.GONE
                    tvSharedRecipesEmpty.visibility = View.GONE
                    layoutSharedBooksHeader.visibility = View.GONE
                    tvSharedBooksEmpty.visibility = View.GONE

                    resultsAdapter.updateData(sortedRecipes)
                    booksAdapter.updateData(sortedBooks)
                }
            }
        })

        val rvSharedRecipes = view.findViewById<RecyclerView>(R.id.rvSharedRecipes)
        val rvSharedBooks = view.findViewById<RecyclerView>(R.id.rvSharedBooks)

        rvSharedRecipes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSharedBooks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        val sharedRecipesAdapter = SearchRecipeAdapter(emptyList()) { item ->
            item.recipe?.let { recipe ->
                val action = SearchFragmentDirections.actionSearchFragmentToRecipeDetailsFragment(
                    recipe = recipe
                )
                findNavController().navigate(action)
            }
        }

        val sharedBooksAdapter = SearchRecipeAdapter(emptyList()) { item ->
            val action = SearchFragmentDirections.actionSearchFragmentToBookRecipesFragment(
                bookId = item.id,
                bookTitle = item.title
            )
            findNavController().navigate(action)
        }

        rvSharedRecipes.adapter = sharedRecipesAdapter
        rvSharedBooks.adapter = sharedBooksAdapter

        recipeViewModel.getSharedWithMeRecipes(uid) { recipes ->
            activity?.runOnUiThread {
                if (recipes.isEmpty()) {
                    tvSharedRecipesEmpty.visibility = View.VISIBLE
                    rvSharedRecipes.visibility = View.GONE
                } else {
                    tvSharedRecipesEmpty.visibility = View.GONE
                    rvSharedRecipes.visibility = View.VISIBLE
                    val items = recipes.map {
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
                    sharedRecipesAdapter.updateData(items)
                }
            }
        }

        bookViewModel.getSharedWithMeBooks(uid) { books ->
            lifecycleScope.launch {
                val trueSharedBooks = books.filter { it.ownerUid != uid }
                val items = trueSharedBooks.map { book ->
                    val recipes = recipeDao.getRecipesByBookId(book.id)
                    SearchItem(
                        id = book.id,
                        title = book.title,
                        type = SearchItemType.BOOK,
                        imageUri = null,
                        bookImages = recipes.take(4).map { it.imageUri }
                    )
                }
                activity?.runOnUiThread {
                    if (trueSharedBooks.isEmpty()) {
                        tvSharedBooksEmpty.visibility = View.VISIBLE
                        rvSharedBooks.visibility = View.GONE
                    } else {
                        tvSharedBooksEmpty.visibility = View.GONE
                        rvSharedBooks.visibility = View.VISIBLE
                        sharedBooksAdapter.updateData(items)
                    }
                }
            }
        }
    }

    private fun showDefaultSections(
        allItems: List<SearchItem>,
        allBooks: List<SearchItem>,
        recentAdapter: SearchRecipeAdapter,
        booksAdapter: SearchRecipeAdapter,
        layoutRecentRecipesHeader: View,
        rvRecentSearch: RecyclerView,
        tvResultsTitle: TextView,
        rvResults: RecyclerView,
        layoutRecentBooksHeader: View,
        tvBooksResultsTitle: TextView,
        rvBooks: RecyclerView,
        layoutFavoriteRecipesHeader: View,
        tvFavoriteRecipesEmpty: TextView,
        layoutFavoriteBooksHeader: View,
        tvFavoriteBooksEmpty: TextView,
        layoutSharedRecipesHeader: View,
        tvSharedRecipesEmpty: TextView,
        layoutSharedBooksHeader: View,
        tvSharedBooksEmpty: TextView,
        layoutDiscoverHeader: View,
        rvDiscover: RecyclerView
    ) {
        val recentRecipeItems = getRecentRecipeIds().mapNotNull { id ->
            allItems.find { it.id == id && it.type == SearchItemType.RECIPE }
        }

        val recentBookItems = getRecentBookIds().mapNotNull { id ->
            allBooks.find { it.id == id && it.type == SearchItemType.BOOK }
        }

        tvResultsTitle.visibility = View.GONE
        rvResults.visibility = View.GONE

        layoutRecentRecipesHeader.visibility = View.VISIBLE
        rvRecentSearch.visibility = View.VISIBLE

        layoutRecentBooksHeader.visibility = View.VISIBLE
        tvBooksResultsTitle.visibility = View.GONE
        rvBooks.visibility = View.VISIBLE

        recentAdapter.updateData(recentRecipeItems)
        booksAdapter.updateData(recentBookItems)

        layoutFavoriteRecipesHeader.visibility = View.VISIBLE
        tvFavoriteRecipesEmpty.visibility = View.VISIBLE
        layoutFavoriteBooksHeader.visibility = View.VISIBLE
        tvFavoriteBooksEmpty.visibility = View.VISIBLE
        layoutSharedRecipesHeader.visibility = View.VISIBLE
        layoutSharedBooksHeader.visibility = View.VISIBLE
        layoutDiscoverHeader.visibility = View.VISIBLE
        rvDiscover.visibility = View.VISIBLE
    }

    private fun saveRecentRecipe(recipeId: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        RecentItemsHelper.saveRecentRecipe(requireContext(), recipeId, uid)
    }

    private fun saveRecentBook(bookId: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        RecentItemsHelper.saveRecentBook(requireContext(), bookId, uid)
    }

    private fun getRecentRecipeIds(): List<Int> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        return RecentItemsHelper.getRecentRecipeIds(requireContext(), uid)
    }

    private fun getRecentBookIds(): List<Int> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        return RecentItemsHelper.getRecentBookIds(requireContext(), uid)
    }
}