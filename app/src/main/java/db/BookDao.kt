package com.example.recipebook.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {

    @Query("SELECT * FROM books")
    fun getAllBooks(): List<BookEntity>

    @Insert
    fun insertBook(book: BookEntity)
}