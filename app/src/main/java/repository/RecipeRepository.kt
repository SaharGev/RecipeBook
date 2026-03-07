package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.RecipeEntity

class RecipeRepository(context: Context) {

    private val recipeDao = DatabaseProvider.getDatabase(context).recipeDao()

    suspend fun getAllRecipes(): List<RecipeEntity> {
        return recipeDao.getAllRecipes()
    }

    suspend fun getRecipesByBookId(bookId: Int): List<RecipeEntity> {
        return recipeDao.getRecipesByBookId(bookId)
    }

    suspend fun insertRecipe(recipe: RecipeEntity) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }
}