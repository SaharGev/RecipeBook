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

    private val repository = CommentRepository(application.applicationContext)

    fun getComments(recipeId: Int, callback: (List<CommentEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val comments = repository.getComments(recipeId)

            withContext(Dispatchers.Main) {
                callback(comments)
            }
        }
    }

    fun addComment(
        recipeId: Int,
        userUid: String,
        username: String,
        text: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addComment(
                CommentEntity(
                    recipeId = recipeId,
                    userUid = userUid,
                    username = username,
                    text = text
                )
            )

            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }
}