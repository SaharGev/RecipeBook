//db/BookEntity
package com.example.recipebook.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val isPublic: Boolean = true,
    val imageUri: String? = null,
    val ownerUid: String = "",
    val sharedWith: String = ""
)