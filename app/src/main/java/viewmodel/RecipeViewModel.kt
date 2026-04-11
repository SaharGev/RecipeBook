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

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application.applicationContext)

    fun getRecipes(uid: String, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes(uid)
            callback(recipes)
        }
    }

    fun getRecipesByBookId(bookId: Int, callback: (List<RecipeEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getRecipesByBookId(bookId)
            callback(recipes)
        }
    }

    fun getRecipesCount(uid: String, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes(uid)
            callback(recipes.size)
        }
    }

    fun deleteRecipeById(uid: String, recipeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getAllRecipes(uid)
            val recipe = recipes.find { it.id == recipeId }
            if (recipe != null) {
                repository.deleteRecipe(recipe)
            }
        }
    }

    fun addRecipe(
        uid: String,
        bookId: Int,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?,
        cookTime: Int,
        difficulty: String,
        isPublic: Boolean,
        onDone: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newRecipe = RecipeEntity(
                bookId = bookId,
                name = name,
                description = description,
                ingredients = ingredients,
                instructions = instructions,
                imageUri = imageUri,
                cookTime = cookTime,
                difficulty = difficulty,
                isPublic = isPublic,
                ownerUid = uid,
                sharedWith = ""
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
            onDone()
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
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

    fun deleteRecipeById(recipeId: Int) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        deleteRecipeById(uid, recipeId)
    }

    fun updateRecipeByFields(
        id: Int,
        bookId: Int,
        name: String,
        description: String,
        ingredients: String,
        instructions: String,
        imageUri: String?,
        cookTime: Int,
        difficulty: String,
        isPublic: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecipe(
                com.example.recipebook.db.RecipeEntity(
                    id = id,
                    bookId = bookId,
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUri = imageUri,
                    cookTime = cookTime,
                    difficulty = difficulty,
                    isPublic = isPublic
                )
            )
        }
    }

    fun saveRecipeToFirestore(recipe: RecipeEntity, uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveRecipeToFirestore(recipe, uid)
        }
    }

    fun getRecipesCountByBookId(bookId: Int, callback: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipes = repository.getRecipesByBookId(bookId)
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
}