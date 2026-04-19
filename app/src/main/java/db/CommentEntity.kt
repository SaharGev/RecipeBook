//db/CommentEntity
package com.example.recipebook.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val userUid: String,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val profileImageUrl: String?
)
