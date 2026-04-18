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
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import com.example.recipebook.utils.RecentItemsHelper

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

    private lateinit var badgeNotifications: View

    private lateinit var btnNotifications: ImageButton

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
        btnNotifications = view.findViewById(R.id.btnNotifications)
        badgeNotifications = view.findViewById(R.id.badgeNotifications)

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

        btnNotifications.setOnClickListener {
            listenToNotifications(uid)
        }

        refreshProfileData()
    }

    override fun onResume() {
        super.onResume()
        refreshProfileData()
    }

    private fun refreshProfileData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        showLoading()
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
        checkForUnseenNotifications(uid)
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
                    hideLoading()
                    if (allBooks.isEmpty()) {
                        rvProfileBooks.visibility = View.GONE
                        tvEmptyProfileBooks.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    rvProfileBooks.visibility = View.VISIBLE
                    tvEmptyProfileBooks.visibility = View.GONE
                }

                val bookImagesMap = mutableMapOf<Int, List<String?>>()
                var remaining = allBooks.size

                allBooks.forEach { book ->
                    recipeViewModel.getRecipesByBookId(book.id) { recipes ->
                        countsMap[book.id] = recipes.size
                        bookImagesMap[book.id] = recipes.take(4).map { it.imageUri }
                        remaining--

                        if (remaining == 0) {
                            rvProfileBooks.post {
                                hideLoading()
                                rvProfileBooks.adapter = RecipeBooksAdapter(
                                    books = allBooks,
                                    onItemClick = { clickedBook ->
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                                        RecentItemsHelper.saveRecentBook(requireContext(), clickedBook.id, uid)
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
                                    countsMap = countsMap,
                                    bookImagesMap = bookImagesMap
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun listenToNotifications(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("invitations")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { invitationsSnapshot ->
                firestore.collection("notifications")
                    .whereEqualTo("toUid", uid)
                    .whereEqualTo("seen", false)
                    .get()
                    .addOnSuccessListener { notificationsSnapshot ->
                        val pendingInvitations = invitationsSnapshot.documents
                        val updateNotifications = notificationsSnapshot.documents

                        if (pendingInvitations.isEmpty() && updateNotifications.isEmpty()) {
                            android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Notifications")
                                .setMessage("No new notifications")
                                .setPositiveButton("OK", null)
                                .show()
                            return@addOnSuccessListener
                        }

                        val items = mutableListOf<String>()
                        var remaining = pendingInvitations.size + updateNotifications.size
                        val invitationLabels = MutableList(pendingInvitations.size) { "" }
                        val notificationLabels = MutableList(updateNotifications.size) { "" }

                        fun tryShowDialog() {
                            if (remaining > 0) return
                            val allItems = (invitationLabels + notificationLabels).toTypedArray()

                            android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Notifications")
                                .setItems(allItems) { _, which ->
                                    if (which < pendingInvitations.size) {
                                        val doc = pendingInvitations[which]
                                        val invitationId = doc.id
                                        val type = doc.getString("type") ?: "book"
                                        val itemName = if (type == "recipe") doc.getString("recipeName") ?: "a recipe"
                                        else doc.getString("bookTitle") ?: "a book"
                                        val fromUid = doc.getString("fromUid") ?: return@setItems
                                        val permission = doc.getString("permission") ?: ""

                                        userViewModel.getUserByUid(fromUid) { fromUser ->
                                            requireActivity().runOnUiThread {
                                                val fromUsername = fromUser?.username ?: "Someone"
                                                android.app.AlertDialog.Builder(requireContext())
                                                    .setTitle("Sharing Request")
                                                    .setMessage("$fromUsername wants to share $type \"$itemName\" with you ($permission access)")
                                                    .setPositiveButton("Accept") { _, _ ->
                                                        bookViewModel.updateInvitationStatus(invitationId, "accepted") {
                                                            requireActivity().runOnUiThread {
                                                                refreshProfileData()
                                                                checkForUnseenNotifications(uid)
                                                            }
                                                        }
                                                    }
                                                    .setNegativeButton("Decline") { _, _ ->
                                                        bookViewModel.updateInvitationStatus(invitationId, "declined")
                                                        checkForUnseenNotifications(uid)
                                                    }
                                                    .show()
                                            }
                                        }
                                    } else {
                                        notificationsSnapshot.documents[which - pendingInvitations.size]
                                            .reference.update("seen", true)
                                        checkForUnseenNotifications(uid)
                                    }
                                }
                                .setPositiveButton("Mark all as read") { _, _ ->
                                    updateNotifications.forEach { it.reference.update("seen", true) }
                                    checkForUnseenNotifications(uid)
                                }
                                .show()
                        }

                        pendingInvitations.forEachIndexed { index, doc ->
                            val fromUid = doc.getString("fromUid") ?: ""
                            val type = doc.getString("type") ?: "book"
                            val itemName = if (type == "recipe") doc.getString("recipeName") ?: "a recipe"
                            else doc.getString("bookTitle") ?: "a book"
                            userViewModel.getUserByUid(fromUid) { fromUser ->
                                val fromUsername = fromUser?.username ?: "Someone"
                                invitationLabels[index] = "📩 $fromUsername wants to share $type \"$itemName\" with you"
                                remaining--
                                requireActivity().runOnUiThread { tryShowDialog() }
                            }
                        }

                        updateNotifications.forEachIndexed { index, doc ->
                            val status = doc.getString("status") ?: ""
                            val itemName = doc.getString("itemName") ?: ""
                            val type = doc.getString("type") ?: "item"
                            val fromUid = doc.getString("fromUid") ?: ""
                            val emoji = if (status == "accepted") "✅" else "❌"
                            userViewModel.getUserByUid(fromUid) { fromUser ->
                                val fromUsername = fromUser?.username ?: "Someone"
                                notificationLabels[index] = "$emoji $fromUsername $status your $type \"$itemName\""
                                remaining--
                                requireActivity().runOnUiThread { tryShowDialog() }
                            }
                        }
                    }
            }
    }

    private fun checkForUnseenNotifications(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("invitations")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { invitationsSnapshot ->
                firestore.collection("notifications")
                    .whereEqualTo("toUid", uid)
                    .whereEqualTo("seen", false)
                    .get()
                    .addOnSuccessListener { notificationsSnapshot ->
                        val hasNew = invitationsSnapshot.documents.isNotEmpty() ||
                                notificationsSnapshot.documents.isNotEmpty()
                        activity?.runOnUiThread {
                            badgeNotifications.visibility = if (hasNew) View.VISIBLE else View.GONE
                        }
                    }
            }
    }

}