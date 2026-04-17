package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri

class UserRepository(context: Context) {

    private val userDao = DatabaseProvider.getDatabase(context).userDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val context = context
    private val storage = FirebaseStorage.getInstance()


    suspend fun getUserByUid(uid: String): UserEntity? {
        val localUser = userDao.getUserByUid(uid)
        if (localUser != null) return localUser
        return getUserFromFirestore(uid)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun saveUserToFirestore(user: UserEntity) {
        firestore.collection("users")
            .document(user.uid)
            .set(
                mapOf(
                    "uid" to user.uid,
                    "username" to user.username,
                    "usernameLower" to user.username.lowercase(),
                    "email" to user.email,
                    "phone" to user.phone,
                    "profileImageUrl" to user.profileImageUrl
                )
            )
            .await()
    }

    suspend fun getUserFromFirestore(uid: String): UserEntity? {
        val document = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        if (!document.exists()) return null

        return UserEntity(
            uid = uid,
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun getOrFetchUser(uid: String): UserEntity? {
        val localUser = userDao.getUserByUid(uid)
        if (localUser != null) return localUser

        val remoteUser = getUserFromFirestore(uid)
        if (remoteUser != null) {
            val existingUser = userDao.getUserByUid(uid)
            if (existingUser == null) {
                userDao.insertUser(remoteUser)
            }
        }

        return remoteUser
    }

    suspend fun getUserByPhone(phone: String): UserEntity? {
        val localUser = userDao.getUserByPhone(phone)
        if (localUser != null) return localUser

        val result = firestore.collection("users")
            .whereEqualTo("phone", phone)
            .get()
            .await()

        val document = result.documents.firstOrNull() ?: return null

        return UserEntity(
            uid = document.getString("uid").orEmpty(),
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun saveUserLocallyAndRemotely(user: UserEntity) {
        val existingUser = userDao.getUserByUid(user.uid)
        if (existingUser == null) {
            userDao.insertUser(user)
        } else {
            userDao.updateUser(user)
        }
        saveUserToFirestore(user)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        val localUser = userDao.getUserByUsername(username)
        if (localUser != null) return localUser

        val result = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        val document = result.documents.firstOrNull() ?: return null

        return UserEntity(
            uid = document.getString("uid").orEmpty(),
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun searchUserByUsernameInFirestore(username: String): UserEntity? {
        val result = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()

        val document = result.documents.firstOrNull() ?: return null

        return UserEntity(
            uid = document.getString("uid").orEmpty(),
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun addFriend(currentUid: String, friendUid: String) {
        val userRef = firestore.collection("users").document(currentUid)

        val document = userRef.get().await()
        val currentFriends = document.get("friends") as? List<String> ?: emptyList()

        if (!currentFriends.contains(friendUid)) {
            userRef.update("friends", currentFriends + friendUid).await()
        }
    }

    suspend fun searchUserByEmailInFirestore(email: String): UserEntity? {
        val result = firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        val document = result.documents.firstOrNull() ?: return null

        return UserEntity(
            uid = document.getString("uid").orEmpty(),
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun searchUserByPhoneInFirestore(phone: String): UserEntity? {
        val result = firestore.collection("users")
            .whereEqualTo("phone", phone)
            .get()
            .await()

        val document = result.documents.firstOrNull() ?: return null

        return UserEntity(
            uid = document.getString("uid").orEmpty(),
            username = document.getString("username").orEmpty(),
            email = document.getString("email").orEmpty(),
            phone = document.getString("phone"),
            profileImageUrl = document.getString("profileImageUrl")
        )
    }

    suspend fun getFriends(currentUid: String): List<UserEntity> {
        val document = firestore.collection("users")
            .document(currentUid)
            .get()
            .await()

        val friendUids = document.get("friends") as? List<String> ?: emptyList()

        return friendUids.mapNotNull { uid ->
            getUserFromFirestore(uid)
        }
    }

    suspend fun getFriendsCount(currentUid: String): Int {
        val document = firestore.collection("users")
            .document(currentUid)
            .get()
            .await()

        val friendUids = document.get("friends") as? List<String> ?: emptyList()
        return friendUids.size
    }

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String {
        val imageRef = storage.reference.child("profile_images/$uid.jpg")

        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw Exception("Cannot open image")

        val bytes = inputStream.readBytes()
        inputStream.close()

        imageRef.putBytes(bytes).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        val localUser = userDao.getUserByEmail(email)
        if (localUser != null) return localUser

        return searchUserByEmailInFirestore(email)
    }

    suspend fun searchUsersByUsernamePrefix(prefix: String): List<UserEntity> {
        val lowerPrefix = prefix.lowercase()
        val result = firestore.collection("users")
            .whereGreaterThanOrEqualTo("usernameLower", lowerPrefix)
            .whereLessThanOrEqualTo("usernameLower", lowerPrefix + "\uF8FF")
            .get()
            .await()

        return result.documents.mapNotNull { document ->
            UserEntity(
                uid = document.getString("uid").orEmpty(),
                username = document.getString("username").orEmpty(),
                email = document.getString("email").orEmpty(),
                phone = document.getString("phone"),
                profileImageUrl = document.getString("profileImageUrl")
            )
        }
    }

    suspend fun removeFriend(currentUid: String, friendUid: String) {
        val userRef = firestore.collection("users").document(currentUid)
        val document = userRef.get().await()
        val currentFriends = document.get("friends") as? List<String> ?: emptyList()
        userRef.update("friends", currentFriends - friendUid).await()
    }

}