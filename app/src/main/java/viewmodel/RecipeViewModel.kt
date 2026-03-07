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

    fun seedIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getAllRecipes()
            if (current.isNotEmpty()) return@launch

            repository.insertRecipe(
                RecipeEntity(
                    bookId = 1,
                    name = "Pasta",
                    description = "Quick and tasty",
                    ingredients = "Pasta, tomato sauce",
                    instructions = "Boil pasta and mix with sauce",
                    imageUri = null
                )
            )

            repository.insertRecipe(
                RecipeEntity(
                    bookId = 1,
                    name = "Salad",
                    description = "Fresh and healthy",
                    ingredients = "Lettuce, tomato, cucumber",
                    instructions = "Chop everything and mix",
                    imageUri = null

                )
            )

            repository.insertRecipe(
                RecipeEntity(
                    bookId = 1,
                    name = "Soup",
                    description = "Warm and comforting",
                    ingredients = "Vegetables, water, salt",
                    instructions = "Boil vegetables for 30 minutes",
                    imageUri = null

                )
            )
        }
    }
    fun getRecipes(callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {

            // Seed first time if DB is empty
            val current = repository.getAllRecipes()
            if (current.isEmpty()) {
                repository.insertRecipe(
                    RecipeEntity(
                        bookId = 1,
                        name = "Pasta",
                        description = "Quick and tasty",
                        ingredients = "Pasta, tomato sauce",
                        instructions = "Boil pasta and mix with sauce",
                        imageUri = null

                    )
                )
                repository.insertRecipe(
                    RecipeEntity(
                        bookId = 1,
                        name = "Salad",
                        description = "Fresh and healthy",
                        ingredients = "Lettuce, tomato, cucumber",
                        instructions = "Chop everything and mix",
                        imageUri = null

                    )
                )
                repository.insertRecipe(
                    RecipeEntity(
                        bookId = 1,
                        name = "Soup",
                        description = "Warm and comforting",
                        ingredients = "Vegetables, water, salt",
                        instructions = "Boil vegetables for 30 minutes",
                        imageUri = null

                    )
                )
            }

            // Fetch again after seeding (or if already existed)
            val recipes = repository.getAllRecipes()
            callback(recipes)
        }
    }

    fun addRecipe(
        bookId: Int,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecipe(
                RecipeEntity(
                    bookId = bookId,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = imageUri
                )
            )
        }
    }
}