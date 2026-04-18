package com.example.recipebook.api

import retrofit2.http.GET
import retrofit2.http.Query

data class MealResponse(val meals: List<Meal>?)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strInstructions: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?
)

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse
    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

}

object MealApi {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val service: MealApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}