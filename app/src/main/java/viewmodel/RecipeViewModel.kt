//viewmodel/RecipeViewModel
package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application.applicationContext)

    fun getRecipes(callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            fun getRecipes(callback: (List<RecipeEntity>) -> Unit) {
                viewModelScope.launch(Dispatchers.IO) {
                    val recipes = repository.getAllRecipes()
                    callback(recipes)
                }
            }

            // Fetch again after seeding (or if already existed)
            val recipes = repository.getAllRecipes()
            callback(recipes)
        }
    }

    fun getRecipesByBookId(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getRecipesByBookId(bookId)
            callback(recipes)
        }
    }

    fun getRecipesCount(callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes()
            callback(recipes.size)
        }
    }

    fun getRecipesCountByBookId(bookId: Int, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getRecipesByBookId(bookId)
            callback(recipes.size)
        }
    }

    fun addRecipe(
        bookId: Int,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?,
        cookTime: Int,
        difficulty: String,
        isPublic: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecipe(
                RecipeEntity(
                    bookId = bookId,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = imageUri,
                    cookTime = cookTime,
                    difficulty = difficulty,
                    isPublic = isPublic
                )
            )
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    fun updateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    fun removeBookFromRecipes(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeBookFromRecipes(bookId)
        }
    }
}