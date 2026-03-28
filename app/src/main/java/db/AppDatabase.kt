package com.example.recipebook.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, BookEntity::class, UserEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun bookDao(): BookDao

    abstract fun userDao(): UserDao
}