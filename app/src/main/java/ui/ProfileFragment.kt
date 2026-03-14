package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnEditProfile = view.findViewById<Button>(R.id.btnEditProfile)
        val btnMyRecipeBooks = view.findViewById<Button>(R.id.btnMyBooks)
        val btnMyRecipes = view.findViewById<Button>(R.id.btnMyRecipes)
        val btnMyFriends = view.findViewById<Button>(R.id.btnMyFriends)

        tvUserName.text = "User Name"
        tvEmail.text = "user@example.com"

        btnEditProfile.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Edit Profile screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnMyRecipeBooks.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        btnMyRecipes.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "My Recipes screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnMyFriends.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "My Friends screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogout.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}