//repository/RecipeRepository
package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class RecipeRepository(context: Context) {

    private val context = context
    private val recipeDao = DatabaseProvider.getDatabase(context).recipeDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAllRecipes(uid: String): List<RecipeEntity> {
        val localRecipes = recipeDao.getAllRecipes()
        if (localRecipes.isNotEmpty()) return localRecipes

        val result = firestore.collection("users")
            .document(uid)
            .collection("recipes")
            .get()
            .await()

        val remoteRecipes = result.documents.mapNotNull { doc ->
            RecipeEntity(
                id = (doc.getLong("id") ?: 0).toInt(),
                bookId = (doc.getLong("bookId") ?: 0).toInt(),
                name = doc.getString("name").orEmpty(),
                description = doc.getString("description").orEmpty(),
                ingredients = doc.getString("ingredients").orEmpty(),
                instructions = doc.getString("instructions").orEmpty(),
                imageUri = doc.getString("imageUri"),
                cookTime = (doc.getLong("cookTime") ?: 0).toInt(),
                difficulty = doc.getString("difficulty").orEmpty(),
                isPublic = doc.getBoolean("isPublic") ?: false
            )
        }

        remoteRecipes.forEach { recipeDao.insertRecipe(it) }
        return remoteRecipes
    }

    suspend fun getRecipesByBookId(bookId: Int): List<RecipeEntity> {
        return recipeDao.getRecipesByBookId(bookId)
    }

    suspend fun insertRecipe(recipe: RecipeEntity): Long {
        return recipeDao.insertRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: RecipeEntity) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun removeBookFromRecipes(bookId: Int) {
        recipeDao.removeBookFromRecipes(bookId)
    }

    suspend fun getRecipeById(id: Int): RecipeEntity? {
        return recipeDao.getRecipeById(id)
    }

    suspend fun saveRecipeToFirestore(recipe: RecipeEntity, uid: String) {
        firestore.collection("users")
            .document(uid)
            .collection("recipes")
            .document(recipe.id.toString())
            .set(
                mapOf(
                    "id" to recipe.id,
                    "bookId" to recipe.bookId,
                    "name" to recipe.name,
                    "description" to recipe.description,
                    "ingredients" to recipe.ingredients,
                    "instructions" to recipe.instructions,
                    "imageUri" to recipe.imageUri,
                    "cookTime" to recipe.cookTime,
                    "difficulty" to recipe.difficulty,
                    "isPublic" to recipe.isPublic
                )
            )
            .await()
    }

    suspend fun uploadRecipeImage(uid: String, recipeId: Int, imageUri: Uri): String {
        val imageRef = storage.reference.child("recipe_images/$uid/$recipeId.jpg")

        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")

        val bytes = inputStream.readBytes()
        inputStream.close()

        imageRef.putBytes(bytes).await()
        return imageRef.downloadUrl.await().toString()
    }
}