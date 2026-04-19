//repository/CommentRepository

package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.CommentEntity
import com.example.recipebook.db.DatabaseProvider
import com.google.firebase.firestore.FirebaseFirestore

class CommentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun addComment(
        recipeId: Int,
        userUid: String,
        username: String,
        text: String,
        profileImageUrl: String?
    ) {
        val comment = hashMapOf(
            "userUid" to userUid,
            "username" to username,
            "text" to text,
            "profileImageUrl" to profileImageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("recipes")
            .document(recipeId.toString())
            .collection("comments")
            .add(comment)
    }

    fun getComments(
        recipeId: Int,
        callback: (List<CommentEntity>) -> Unit
    ) {
        firestore.collection("recipes")
            .document(recipeId.toString())
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.map {
                    CommentEntity(
                        id = 0,
                        recipeId = recipeId,
                        userUid = it.getString("userUid") ?: "",
                        username = it.getString("username") ?: "",
                        text = it.getString("text") ?: "",
                        profileImageUrl = it.getString("profileImageUrl"),
                        timestamp = it.getLong("timestamp") ?: 0L
                    )
                }

                callback(list)
            }
    }
}