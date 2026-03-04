package com.example.recipebook.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): List<RecipeEntity>

    @Insert
    fun insertRecipe(recipe: RecipeEntity)
}