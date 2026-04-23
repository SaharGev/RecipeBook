//db/RecipeEntity
package com.example.recipebook.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val ingredients: String,
    val instructions: String,
    val imageUri: String?,
    val cookTime: Int,
    val difficulty: String,
    val isPublic: Boolean,
    val ownerUid: String = "",
    val sharedWith: String = ""
)
