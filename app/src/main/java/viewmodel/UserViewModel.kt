package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.UserEntity
import com.example.recipebook.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri

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

    fun searchUserByUsername(username: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.searchUserByUsernameInFirestore(username)
            callback(user)
        }
    }

    fun addFriend(currentUid: String, friendUid: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addFriend(currentUid, friendUid)
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }

    fun searchUser(query: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(query).matches()
            val isPhone = query.all { it.isDigit() || it == '+' || it == '-' }

            val user = when {
                isEmail -> repository.searchUserByEmailInFirestore(query)
                isPhone -> repository.searchUserByPhoneInFirestore(query)
                else -> repository.searchUserByUsernameInFirestore(query)
            }

            callback(user)
        }
    }

    fun getFriends(currentUid: String, callback: (List<UserEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val friends = repository.getFriends(currentUid)
            callback(friends)
        }
    }

    fun getFriendsCount(currentUid: String, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getFriendsCount(currentUid)
            callback(count)
        }
    }

    fun uploadProfileImage(uid: String, imageUri: Uri, onDone: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = repository.uploadProfileImage(uid, imageUri)
                onDone(url)
            } catch (e: Exception) {
                android.util.Log.e("DEBUG", "Profile image upload failed: ${e.message}", e)
                onDone("")
            }
        }
    }

    fun getUserByEmail(email: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            callback(user)
        }
    }

    fun searchUsersByUsernamePrefix(prefix: String, callback: (List<UserEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val users = repository.searchUsersByUsernamePrefix(prefix)
            callback(users)
        }
    }

    fun removeFriend(currentUid: String, friendUid: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.removeFriend(currentUid, friendUid)
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            }
        }
    }
}