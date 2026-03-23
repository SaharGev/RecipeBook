//repository/BookRepository
package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.BookEntity
import com.example.recipebook.db.DatabaseProvider

class BookRepository(context: Context) {

    private val bookDao = DatabaseProvider.getDatabase(context).bookDao()

    suspend fun getAllBooks(): List<BookEntity> {
        return bookDao.getAllBooks()
    }

    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun deleteBook(bookId: Int) {
        bookDao.deleteBook(bookId)
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }
}