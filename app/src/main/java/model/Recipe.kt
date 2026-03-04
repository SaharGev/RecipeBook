package com.example.recipebook.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val ingredients: String,
    val instructions: String
) : Parcelable