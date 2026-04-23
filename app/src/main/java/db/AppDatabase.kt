// db/AppDataBase
package com.example.recipebook.db
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecipeEntity::class, BookEntity::class,
                UserEntity::class, CommentEntity::class,
                RecipeBookCrossRef::class],
    version = 12
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao
    abstract fun recipeBookDao(): RecipeBookDao
}