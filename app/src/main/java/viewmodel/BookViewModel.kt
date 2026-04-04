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

    fun addBook(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBook(
                BookEntity(title = title)
            )
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