package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.BookEntity
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.repository.BookRepository
import com.example.recipebook.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRecipeBookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository = BookRepository(application.applicationContext)
    private val recipeRepository = RecipeRepository(application.applicationContext)

    fun getAllBooks(callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            callback(bookRepository.getAllBooks())
        }
    }

    fun getAvailableRecipesForBook(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val allRecipes = recipeRepository.getAllRecipes()
            val filteredRecipes = allRecipes.filter { it.bookId != bookId }
            callback(filteredRecipes)
        }
    }

    fun createBook(
        title: String,
        description: String,
        isPublic: Boolean,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.insertBook(
                BookEntity(
                    title = title,
                    description = description,
                    isPublic = isPublic
                )
            )
            onDone?.invoke()
        }
    }

    fun addRecipeToBook(
        recipe: RecipeEntity,
        bookId: Int,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecipe = recipe.copy(bookId = bookId)
            recipeRepository.updateRecipe(updatedRecipe)
            onDone?.invoke()
        }
    }
}