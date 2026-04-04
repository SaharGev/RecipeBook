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

}