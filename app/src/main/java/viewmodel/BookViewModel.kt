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

    private val _books = androidx.lifecycle.MutableLiveData<List<BookEntity>>()
    val books: androidx.lifecycle.LiveData<List<BookEntity>> = _books

    fun loadBooks(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllBooks(uid)
            _books.postValue(result)
        }
    }

    fun getBooks(uid: String, callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = repository.getAllBooks(uid)
            callback(books)
        }
    }

    fun getBooksCount(uid: String, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = repository.getAllBooks(uid)
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
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            repository.deleteBook(bookId)
            repository.deleteBookFromFirestore(bookId, uid)
        }
    }

    fun updateBook(bookId: Int, title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(bookId, title, description)
        }
    }

    fun deleteBookAndDetachRecipes(bookId: Int, recipeViewModel: RecipeViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeViewModel.removeBookFromRecipes(bookId)
            repository.deleteBook(bookId)
        }
    }

    fun getSharedWithMeBooks(uid: String, callback: (List<BookEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = repository.getSharedWithMeBooks(uid)
            callback(books)
        }
    }

    fun listenToPendingInvitations(uid: String, callback: (List<Map<String, Any>>) -> Unit) {
        repository.listenToPendingInvitations(uid, callback)
    }

    fun updateInvitationStatus(invitationId: String, status: String, onDone: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateInvitationStatus(invitationId, status)
            onDone()
        }
    }

    fun getBookById(bookId: Int, uid: String, callback: (BookEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = repository.getBookById(bookId, uid)
            callback(book)
        }
    }
}