package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.BookEntity
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.repository.BookRepository
import com.example.recipebook.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRecipeBookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository = BookRepository(application.applicationContext)

    private val userRepository = com.example.recipebook.repository.UserRepository(application.applicationContext)
    private val recipeRepository = RecipeRepository(application.applicationContext)

    fun getAllBooks(callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            callback(bookRepository.getAllBooks(uid))
        }
    }

    fun getAvailableRecipesForBook(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val allRecipes = recipeRepository.getAllRecipes()
            val filteredRecipes = allRecipes.filter { it.bookId != bookId }
            callback(filteredRecipes)
        }
    }

    fun createBook(
        title: String,
        description: String,
        isPublic: Boolean,
        imageUri: String? = null,
        sharedWith: String = "",
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            val newBook = BookEntity(
                title = title,
                description = description,
                isPublic = isPublic,
                imageUri = imageUri,
                ownerUid = uid,
                sharedWith = sharedWith
            )

            val id = bookRepository.insertBook(newBook)

            val finalImageUrl = if (imageUri != null) {
                try {
                    bookRepository.uploadBookImage(uid, id.toInt(), android.net.Uri.parse(imageUri))
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG", "Book image upload failed: ${e.message}", e)
                    imageUri
                }
            } else null

            val savedBook = newBook.copy(id = id.toInt(), imageUri = finalImageUrl)
            bookRepository.saveBookToFirestore(savedBook, uid)
            onDone?.invoke()
        }
    }

    fun addRecipeToBook(
        recipe: RecipeEntity,
        bookId: Int,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecipe = recipe.copy(bookId = bookId)
            recipeRepository.updateRecipe(updatedRecipe)
            onDone?.invoke()
        }
    }

    fun getFriends(uid: String, callback: (List<com.example.recipebook.db.UserEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val friends = userRepository.getFriends(uid)
            callback(friends)
        }
    }
}