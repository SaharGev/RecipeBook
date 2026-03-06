package com.example.recipebook.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {

        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "recipe_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        return instance!!
    }
}