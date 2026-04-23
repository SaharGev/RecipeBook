//db/RecipeBookCrossRef
package com.example.recipebook.db

import androidx.room.Entity

@Entity(primaryKeys = ["recipeId", "bookId"])
data class RecipeBookCrossRef(
    val recipeId: Int,
    val bookId: Int
)
