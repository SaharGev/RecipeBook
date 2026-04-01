//ui/AddNavbarFragment
package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipebook.R

class AddNavbarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_add_navbar, container, false)

        val cardAddRecipe = view.findViewById<LinearLayout>(R.id.cardAddRecipe)
        val cardAddBook = view.findViewById<LinearLayout>(R.id.cardAddBook)

        cardAddRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_addNavbarFragment_to_addRecipeFragment)
        }

        cardAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_addNavbarFragment_to_addRecipeBookFragment)
        }

        return view
    }
}