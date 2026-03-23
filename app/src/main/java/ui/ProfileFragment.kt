//ui/ProfileFragment
package com.example.recipebook.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipebook.R
import com.example.recipebook.viewmodel.BookViewModel
import com.example.recipebook.viewmodel.RecipeViewModel
import com.google.android.material.imageview.ShapeableImageView

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val bookViewModel: BookViewModel by viewModels()

    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRecipesCount: TextView
    private lateinit var tvBooksCount: TextView
    private lateinit var tvFriendsCount: TextView
    private lateinit var tvEmptyProfileBooks: TextView
    private lateinit var imgProfile: ShapeableImageView
    private lateinit var rvProfileBooks: RecyclerView
    private lateinit var btnSettings: ImageButton
    private lateinit var statRecipes: View
    private lateinit var statBooks: View
    private lateinit var statFriends: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvEmail)

        statRecipes = view.findViewById(R.id.statRecipes)
        statBooks = view.findViewById(R.id.statBooks)
        statFriends = view.findViewById(R.id.statFriends)

        btnSettings = view.findViewById(R.id.btnSettings)

        tvRecipesCount = view.findViewById(R.id.tvRecipesCount)
        tvBooksCount = view.findViewById(R.id.tvBooksCount)
        tvFriendsCount = view.findViewById(R.id.tvFriendsCount)

        tvEmptyProfileBooks = view.findViewById(R.id.tvEmptyProfileBooks)
        imgProfile = view.findViewById(R.id.imgProfile)

        rvProfileBooks = view.findViewById(R.id.rvProfileBooks)
        rvProfileBooks.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProfileBooks.isNestedScrollingEnabled = false

        tvUserName.text = "User Name"
        tvEmail.text = "user@example.com"
        tvFriendsCount.text = "0"

        statBooks.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        statRecipes.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "My Recipes screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        statFriends.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Friends screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        refreshProfileData()
    }

    override fun onResume() {
        super.onResume()
        refreshProfileData()
    }

    private fun refreshProfileData() {
        loadSavedProfileImage(imgProfile)

        recipeViewModel.getRecipesCount { count ->
            activity?.runOnUiThread {
                tvRecipesCount.text = count.toString()
            }
        }

        bookViewModel.getBooksCount { count ->
            activity?.runOnUiThread {
                tvBooksCount.text = count.toString()
            }
        }

        loadProfileBooks(rvProfileBooks, tvEmptyProfileBooks)
    }

    private fun loadSavedProfileImage(imageView: ShapeableImageView) {
        val prefs = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedImageUri = prefs.getString("profile_image_uri", null)

        if (!savedImageUri.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(savedImageUri))
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    private fun loadProfileBooks(rvProfileBooks: RecyclerView, tvEmptyProfileBooks: TextView) {
        bookViewModel.getBooks { books ->
            val countsMap = mutableMapOf<Int, Int>()

            if (books.isEmpty()) {
                rvProfileBooks.post {
                    rvProfileBooks.visibility = View.GONE
                    tvEmptyProfileBooks.visibility = View.VISIBLE
                }
                return@getBooks
            }

            rvProfileBooks.post {
                rvProfileBooks.visibility = View.VISIBLE
                tvEmptyProfileBooks.visibility = View.GONE
            }

            var remaining = books.size

            books.forEach { book ->
                recipeViewModel.getRecipesCountByBookId(book.id) { count ->
                    countsMap[book.id] = count
                    remaining--

                    if (remaining == 0) {
                        rvProfileBooks.post {
                            rvProfileBooks.adapter = RecipeBooksAdapter(
                                books = books,
                                onItemClick = { clickedBook ->
                                    val bundle = Bundle()
                                    bundle.putInt("bookId", clickedBook.id)
                                    bundle.putString("bookTitle", clickedBook.title)

                                    findNavController().navigate(
                                        R.id.action_profileFragment_to_bookRecipesFragment,
                                        bundle
                                    )
                                },
                                countsMap = countsMap
                            )
                        }
                    }
                }
            }
        }
    }
}