package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.RecipeBookCrossRef
import com.example.recipebook.db.RecipeEntity
import kotlinx.coroutines.tasks.await

class RecipeBookRepository(context: Context) {

    private val db = DatabaseProvider.getDatabase(context)
    private val dao = db.recipeBookDao()

    suspend fun addRecipeToBook(recipeId: Int, bookId: Int) {
        dao.addRecipeToBook(
            RecipeBookCrossRef(recipeId, bookId)
        )
    }

    suspend fun removeRecipeFromBook(recipeId: Int, bookId: Int) {
        dao.removeRecipeFromBook(recipeId, bookId)
    }

    suspend fun getRecipesForBook(bookId: Int): List<RecipeEntity> {
        return dao.getRecipesForBook(bookId)
    }

    suspend fun addRecipeToBookFirestore(uid: String, bookId: Int, recipeId: Int) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("books")
            .document(bookId.toString())
            .collection("recipes")
            .document(recipeId.toString())
            .set(
                mapOf(
                    "recipeId" to recipeId,
                    "addedAt" to com.google.firebase.Timestamp.now()
                )
            )
            .await()
    }
}