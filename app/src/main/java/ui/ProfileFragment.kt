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
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.recipebook.viewmodel.UserViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val userViewModel: UserViewModel by viewModels()
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

        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        userViewModel.getUserByUid(uid) { user ->
            activity?.runOnUiThread {
                tvUserName.text = user?.username ?: "User Name"
                tvEmail.text = user?.email ?: ""
            }
        }

        userViewModel.getFriendsCount(uid) { count ->
            activity?.runOnUiThread {
                tvFriendsCount.text = count.toString()
            }
        }

        statBooks.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        statRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_myRecipesFragment)
        }

        statFriends.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_friendsFragment)
        }

        btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        refreshProfileData()
        bookViewModel.listenToPendingInvitations(uid) { invitations ->
            if (invitations.isEmpty()) return@listenToPendingInvitations
            if (!isAdded) return@listenToPendingInvitations
            requireActivity().runOnUiThread {
                if (!isAdded) return@runOnUiThread
                showInvitationDialog(invitations.first())
            }
        }

    }

    override fun onResume() {
        super.onResume()
        refreshProfileData()
    }

    private fun refreshProfileData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        loadSavedProfileImage(imgProfile)

        recipeViewModel.getRecipesCount(uid) { count ->
            activity?.runOnUiThread {
                tvRecipesCount.text = count.toString()
            }
        }

        bookViewModel.getBooksCount(uid) { count ->
            activity?.runOnUiThread {
                tvBooksCount.text = count.toString()
            }
        }

        loadProfileBooks(rvProfileBooks, tvEmptyProfileBooks)
    }

    private fun loadSavedProfileImage(imageView: ShapeableImageView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        userViewModel.getUserByUid(uid) { user ->
            activity?.runOnUiThread {
                if (!user?.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(user?.profileImageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }
        }
    }

    private fun loadProfileBooks(rvProfileBooks: RecyclerView, tvEmptyProfileBooks: TextView) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        bookViewModel.getBooks(uid) { myBooks ->
            bookViewModel.getSharedWithMeBooks(uid) { sharedBooks ->
                val myOwnBooks = myBooks.filter { it.ownerUid == uid }
                val allBooks = (myOwnBooks + sharedBooks).distinctBy { it.id }
                val countsMap = mutableMapOf<Int, Int>()

                activity?.runOnUiThread {
                    if (allBooks.isEmpty()) {
                        rvProfileBooks.visibility = View.GONE
                        tvEmptyProfileBooks.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    rvProfileBooks.visibility = View.VISIBLE
                    tvEmptyProfileBooks.visibility = View.GONE
                }

                var remaining = allBooks.size

                allBooks.forEach { book ->
                    recipeViewModel.getRecipesCountByBookId(book.id) { count ->
                        countsMap[book.id] = count
                        remaining--

                        if (remaining == 0) {
                            rvProfileBooks.post {
                                rvProfileBooks.adapter = RecipeBooksAdapter(
                                    books = allBooks,
                                    onItemClick = { clickedBook ->
                                        val bundle = Bundle()
                                        bundle.putInt("bookId", clickedBook.id)
                                        bundle.putString("bookTitle", clickedBook.title)
                                        findNavController().navigate(
                                            R.id.action_profileFragment_to_bookRecipesFragment,
                                            bundle
                                        )
                                    },
                                    onDeleteClick = { book ->
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            bookViewModel.deleteBook(book.id)
                                            refreshProfileData()
                                        }
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

    private fun showInvitationDialog(invitation: Map<String, Any>) {
        val invitationId = invitation["invitationId"] as? String ?: return
        val fromUid = invitation["fromUid"] as? String ?: return
        val permission = invitation["permission"] as? String ?: return
        val type = invitation["type"] as? String ?: "book"

        val itemName = if (type == "recipe") {
            invitation["recipeName"] as? String ?: "a recipe"
        } else {
            invitation["bookTitle"] as? String ?: "a book"
        }

        userViewModel.getUserByUid(fromUid) { fromUser ->
            requireActivity().runOnUiThread {
                val fromUsername = fromUser?.username ?: "Someone"
                val itemType = if (type == "recipe") "recipe" else "book"

                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Sharing Request")
                    .setMessage("$fromUsername wants to share $itemType \"$itemName\" with you ($permission access)")
                    .setPositiveButton("Accept") { _, _ ->
                        bookViewModel.updateInvitationStatus(invitationId, "accepted") {
                            requireActivity().runOnUiThread {
                                refreshProfileData()
                            }
                        }
                    }
                    .setNegativeButton("Decline") { _, _ ->
                        bookViewModel.updateInvitationStatus(invitationId, "declined")
                    }
                    .show()
            }
        }
    }
}