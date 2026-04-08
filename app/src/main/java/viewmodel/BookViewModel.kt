//viewmodel/BookViewModel
package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.BookEntity
import com.example.recipebook.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookRepository(application.applicationContext)

    fun getBooks(callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = repository.getAllBooks()
            callback(books)
        }
    }

    fun getBooksCount(callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = repository.getAllBooks()
            callback(books.size)
        }
    }

    fun addBook(
        uid: String,
        title: String,
        description: String = "",
        isPublic: Boolean = true,
        imageUri: String? = null,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBook = BookEntity(
                title = title,
                description = description,
                isPublic = isPublic,
                imageUri = imageUri,
                ownerUid = uid
            )

            val id = repository.insertBook(newBook)

            val finalImageUrl = if (imageUri != null) {
                try {
                    repository.uploadBookImage(uid, id.toInt(), android.net.Uri.parse(imageUri))
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG", "Book image upload failed: ${e.message}", e)
                    imageUri
                }
            } else null

            val savedBook = newBook.copy(id = id.toInt(), imageUri = finalImageUrl)
            repository.saveBookToFirestore(savedBook, uid)
            onDone()
        }
    }

    fun deleteBook(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBook(bookId)
        }
    }

    fun updateBook(bookId: Int, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(
                BookEntity(
                    id = bookId,
                    title = newTitle
                )
            )
        }
    }

    fun deleteBookAndDetachRecipes(bookId: Int, recipeViewModel: RecipeViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeViewModel.removeBookFromRecipes(bookId)
            repository.deleteBook(bookId)
        }
    }
}