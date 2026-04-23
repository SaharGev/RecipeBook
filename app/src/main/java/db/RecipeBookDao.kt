//db/RecipeBookDao
package com.example.recipebook.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeBookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecipeToBook(crossRef: RecipeBookCrossRef)

    @Query("DELETE FROM RecipeBookCrossRef WHERE bookId = :bookId AND recipeId = :recipeId")
    suspend fun removeRecipeFromBook(bookId: Int, recipeId: Int)

    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN RecipeBookCrossRef rb ON r.id = rb.recipeId
        WHERE rb.bookId = :bookId
    """)
    suspend fun getRecipesForBook(bookId: Int): List<RecipeEntity>
}