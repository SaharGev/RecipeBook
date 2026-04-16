//db/CommentDao
package com.example.recipebook.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE recipeId = :recipeId ORDER BY timestamp DESC")
    suspend fun getCommentsByRecipeId(recipeId: Int): List<CommentEntity>

    @Insert
    suspend fun insertComment(comment: CommentEntity)
}