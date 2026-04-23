package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.BookEntity
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.db.UserEntity
import com.example.recipebook.repository.BookRepository
import com.example.recipebook.repository.RecipeRepository
import com.example.recipebook.repository.RecipeBookRepository
import com.example.recipebook.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRecipeBookViewModel(application: Application) : AndroidViewModel(application) {

    private val bookRepository = BookRepository(application.applicationContext)
    private val recipeRepository = RecipeRepository(application.applicationContext)
    private val recipeBookRepository = RecipeBookRepository(application.applicationContext)
    private val userRepository = UserRepository(application.applicationContext)

    fun getAllBooks(callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            callback(bookRepository.getAllBooks(uid))
        }
    }

    fun getAvailableRecipesForBook(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            val allRecipes = recipeRepository.getAllRecipes(uid)
            val recipesInBook = recipeBookRepository.getRecipesForBook(bookId)

            val available = allRecipes.filter { recipe ->
                recipesInBook.none { it.id == recipe.id }
            }

            callback(available)
        }
    }

    fun addRecipeToBook(recipeId: Int, bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            recipeBookRepository.addRecipeToBook(recipeId, bookId)
            recipeBookRepository.addRecipeToBookFirestore(uid, bookId, recipeId)
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
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            val newBook = BookEntity(
                title = title,
                description = description,
                isPublic = isPublic,
                imageUri = imageUri,
                ownerUid = uid,
                sharedWith = sharedWith
            )

            val id = bookRepository.insertBook(newBook)

            val savedBook = newBook.copy(id = id.toInt(), imageUri = imageUri)
            bookRepository.saveBookToFirestore(savedBook, uid)

            onDone?.invoke()
        }
    }

    fun getFriends(uid: String, callback: (List<UserEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            callback(userRepository.getFriends(uid))
        }
    }
}