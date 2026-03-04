package com.example.recipebook.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>


    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity)
}