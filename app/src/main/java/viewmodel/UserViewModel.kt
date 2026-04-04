package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.UserEntity
import com.example.recipebook.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application.applicationContext)

    fun getUserByUid(uid: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUid(uid)
            callback(user)
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUser(user)
        }
    }

    fun saveUserToFirestore(user: UserEntity, onDone: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserToFirestore(user)
            onDone?.invoke()
        }
    }

    fun getUserFromFirestore(uid: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserFromFirestore(uid)
            callback(user)
        }
    }

    fun getOrFetchUser(uid: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getOrFetchUser(uid)
            callback(user)
        }
    }

    fun getUserByPhone(phone: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByPhone(phone)
            callback(user)
        }
    }

    fun saveUserLocallyAndRemotely(user: UserEntity, onDone: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserLocallyAndRemotely(user)
            onDone?.invoke()
        }
    }

    fun getUserByUsername(username: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByUsername(username)
            callback(user)
        }
    }
}