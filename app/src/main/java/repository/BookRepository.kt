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

        remoteBooks.forEach { book ->
            val existing = bookDao.getAllBooks()
            if (existing.none { it.id == book.id }) {
                bookDao.insertBook(book)
            }
        }
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

    suspend fun getSharedWithMeBooks(myUid: String): List<BookEntity> {
        val acceptedInvitations = firestore.collection("invitations")
            .whereEqualTo("toUid", myUid)
            .whereEqualTo("status", "accepted")
            .get()
            .await()

        val sharedBooks = mutableListOf<BookEntity>()

        for (doc in acceptedInvitations.documents) {
            val ownerUid = doc.getString("fromUid") ?: continue
            val bookId = (doc.getLong("bookId") ?: continue).toInt()

            val bookDoc = firestore.collection("users")
                .document(ownerUid)
                .collection("books")
                .document(bookId.toString())
                .get()
                .await()

            if (!bookDoc.exists()) continue

            sharedBooks.add(
                BookEntity(
                    id = (bookDoc.getLong("id") ?: 0).toInt(),
                    title = bookDoc.getString("title").orEmpty(),
                    description = bookDoc.getString("description").orEmpty(),
                    isPublic = bookDoc.getBoolean("isPublic") ?: true,
                    imageUri = bookDoc.getString("imageUri"),
                    ownerUid = bookDoc.getString("ownerUid").orEmpty(),
                    sharedWith = (bookDoc.get("sharedWith") as? List<String>)?.joinToString(",").orEmpty()
                )
            )
        }

        return sharedBooks
    }

    suspend fun sendBookInvitations(book: BookEntity, ownerUid: String) {
        val sharedWithList = book.sharedWith.split(",").filter { it.isNotEmpty() }

        sharedWithList.forEach { entry ->
            val parts = entry.split(":")
            if (parts.size != 2) return@forEach

            val toUid = parts[0]
            val permission = parts[1]

            val invitation = hashMapOf(
                "fromUid" to ownerUid,
                "toUid" to toUid,
                "bookId" to book.id,
                "bookTitle" to book.title,
                "permission" to permission,
                "status" to "pending"
            )

            firestore.collection("invitations")
                .add(invitation)
                .await()
        }
    }

    fun listenToPendingInvitations(uid: String, onInvitation: (List<Map<String, Any>>) -> Unit) {
        firestore.collection("invitations")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val invitations = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("invitationId" to doc.id)
                }

                onInvitation(invitations)
            }
    }

    suspend fun updateInvitationStatus(invitationId: String, status: String) {
        firestore.collection("invitations")
            .document(invitationId)
            .update("status", status)
            .await()
    }
}