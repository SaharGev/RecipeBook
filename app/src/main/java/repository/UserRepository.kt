package com.example.recipebook.repository

import android.content.Context
import com.example.recipebook.db.DatabaseProvider
import com.example.recipebook.db.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(context: Context) {

    private val userDao = DatabaseProvider.getDatabase(context).userDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserByUid(uid: String): UserEntity? {
        return userDao.getUserByUid(uid)
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
        return userDao.getUserByPhone(phone)
    }

    suspend fun saveUserLocallyAndRemotely(user: UserEntity) {
        val existingUser = userDao.getUserByUid(user.uid)
        if (existingUser == null) {
            userDao.insertUser(user)
        }

        saveUserToFirestore(user)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
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
}