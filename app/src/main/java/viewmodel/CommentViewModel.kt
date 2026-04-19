//viewmodel/CommentViewModel
package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.CommentEntity
import com.example.recipebook.repository.CommentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CommentRepository()

    fun addComment(
        recipeId: Int,
        userUid: String,
        username: String,
        text: String,
        profileImageUrl: String?,
        onDone: () -> Unit = {}
    ) {
        repository.addComment(recipeId, userUid, username, text, profileImageUrl)
        onDone()
    }

    fun getComments(recipeId: Int, callback: (List<CommentEntity>) -> Unit) {
        repository.getComments(recipeId, callback)
    }
}