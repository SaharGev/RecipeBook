//viewmodel/RecipeViewModel
package com.example.recipebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.db.RecipeEntity
import com.example.recipebook.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application.applicationContext)

    private val _recipes = androidx.lifecycle.MutableLiveData<List<RecipeEntity>>()
    val recipes: androidx.lifecycle.LiveData<List<RecipeEntity>> = _recipes

    fun loadRecipes(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.getAllRecipes(uid)
            _recipes.postValue(result)
        }
    }

    fun getRecipes(uid: String, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes(uid)
            callback(recipes)
        }
    }

    fun getRecipesByBookId(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            val recipes = repository.getRecipesByBookFromFirestore(uid, bookId)

            callback(recipes)
        }
    }

    fun getRecipesCount(uid: String, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes(uid)
            val myOwnRecipes = recipes.filter { it.ownerUid == uid }
            callback(myOwnRecipes.size)
        }
    }

    fun addRecipe(
        uid: String,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?,
        cookTime: Int,
        difficulty: String,
        isPublic: Boolean,
        sharedWith: String = "",
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRecipe = RecipeEntity(
                name = name,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                imageUri = imageUri,
                cookTime = cookTime,
                difficulty = difficulty,
                isPublic = isPublic,
                ownerUid = uid,
                sharedWith = sharedWith
            )

            val id = repository.insertRecipe(newRecipe)

            val finalImageUrl = if (imageUri != null) {
                try {
                    repository.uploadRecipeImage(uid, id.toInt(), Uri.parse(imageUri))
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG", "Upload failed: ${e.message}", e)
                    imageUri
                }
            } else null

            val savedRecipe = newRecipe.copy(id = id.toInt(), imageUri = finalImageUrl)
            repository.saveRecipeToFirestore(savedRecipe, uid)

            if (sharedWith.isNotEmpty()) {
                repository.sendRecipeInvitations(savedRecipe, uid)
            }

            onDone()
        }
    }

    fun updateRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(recipe)
        }
    }

    fun removeBookFromRecipes(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeBookFromRecipes(bookId)
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            repository.deleteRecipe(recipe, uid)

            val updated = repository.getAllRecipes(uid)
            _recipes.postValue(updated)
        }
    }

    fun updateRecipeByFields(
        id: Int,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?,
        cookTime: Int,
        difficulty: String,
        isPublic: Boolean,
        sharedWith: String = "",
        uid: String = "",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedRecipe = RecipeEntity(
                id = id,
                name = name,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                imageUri = imageUri,
                cookTime = cookTime,
                difficulty = difficulty,
                isPublic = isPublic,
                ownerUid = uid,
                sharedWith = sharedWith
            )

            repository.updateRecipe(updatedRecipe)

            if (uid.isNotEmpty()) {
                repository.saveRecipeToFirestore(updatedRecipe, uid)
                if (sharedWith.isNotEmpty()) {
                    repository.sendRecipeInvitations(updatedRecipe, uid)
                }
            }

            onDone()
        }
    }

    fun saveRecipeToFirestore(recipe: RecipeEntity, uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveRecipeToFirestore(recipe, uid)
        }
    }

    fun getRecipesCountByBookId(bookId: Int, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val recipes  = repository.getRecipesByBookFromFirestore(uid, bookId)
            callback(recipes.size)
        }
    }

    fun getSharedWithMeRecipes(uid: String, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getSharedWithMeRecipes(uid)
            callback(recipes)
        }
    }

    fun sendRecipeInvitations(recipe: com.example.recipebook.db.RecipeEntity, uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendRecipeInvitations(recipe, uid)
        }
    }

    fun getRecipesByBook(bookId: Int, uid: String, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getRecipesByBookFromFirestore(uid, bookId)
            callback(recipes)
        }
    }


}