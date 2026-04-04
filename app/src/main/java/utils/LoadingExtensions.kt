package com.example.recipebook.utils

import androidx.fragment.app.Fragment
import com.example.recipebook.MainActivity

fun Fragment.showLoading() {
    (requireActivity() as MainActivity).showLoading()
}

fun Fragment.hideLoading() {
    (requireActivity() as MainActivity).hideLoading()
}