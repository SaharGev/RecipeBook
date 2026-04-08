package com.example.recipebook.repository

import android.content.Context
import android.net.Uri
import com.example.recipebook.db.BookEntity
import com.example.recipebook.db.DatabaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class BookRepository(context: Context) {

    private val context = context
    private val bookDao = DatabaseProvider.getDatabase(context).bookDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getAllBooks(uid: String): List<BookEntity> {
        val localBooks = bookDao.getAllBooks()
        if (localBooks.isNotEmpty()) return localBooks

        val result = firestore.collection("users")
            .document(uid)
            .collection("books")
            .get()
            .await()

        val remoteBooks = result.documents.mapNotNull { doc ->
            BookEntity(
                id = (doc.getLong("id") ?: 0).toInt(),
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                isPublic = doc.getBoolean("isPublic") ?: true,
                imageUri = doc.getString("imageUri"),
                ownerUid = doc.getString("ownerUid").orEmpty(),
                sharedWith = (doc.get("sharedWith") as? List<String>)?.joinToString(",").orEmpty()
            )
        }

        remoteBooks.forEach { bookDao.insertBook(it) }
        return remoteBooks
    }

    suspend fun insertBook(book: BookEntity): Long {
        return bookDao.insertBook(book)
    }

    suspend fun deleteBook(bookId: Int) {
        bookDao.deleteBook(bookId)
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }

    suspend fun saveBookToFirestore(book: BookEntity, uid: String) {
        firestore.collection("users")
            .document(uid)
            .collection("books")
            .document(book.id.toString())
            .set(
                mapOf(
                    "id" to book.id,
                    "title" to book.title,
                    "description" to book.description,
                    "isPublic" to book.isPublic,
                    "imageUri" to book.imageUri,
                    "ownerUid" to uid,
                    "sharedWith" to book.sharedWith.split(",").filter { it.isNotEmpty() }
                )
            )
            .await()
    }

    suspend fun uploadBookImage(uid: String, bookId: Int, imageUri: Uri): String {
        val imageRef = storage.reference.child("book_images/$uid/$bookId.jpg")

        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")

        val bytes = inputStream.readBytes()
        inputStream.close()

        imageRef.putBytes(bytes).await()
        return imageRef.downloadUrl.await().toString()
    }
}