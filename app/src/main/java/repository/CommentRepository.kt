//repository/CommentRepository

package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.CommentEntity
import com.example.recipebook.db.DatabaseProvider

class CommentRepository(context: Context) {

    private val commentDao = DatabaseProvider.getDatabase(context).commentDao()

    suspend fun getComments(recipeId: Int): List<CommentEntity> {
        return commentDao.getCommentsByRecipeId(recipeId)
    }

    suspend fun addComment(comment: CommentEntity) {
        commentDao.insertComment(comment)
    }
}