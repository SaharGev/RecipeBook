package com.example.recipebook.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.UserEntity
import com.example.recipebook.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import com.example.recipebook.utils.showLoading
import com.example.recipebook.utils.hideLoading
import kotlinx.coroutines.launch

class FriendsFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private var foundUser: UserEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearchFriend = view.findViewById<TextInputEditText>(R.id.etSearchFriend)
        val cardSearchResult = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardSearchResult)
        val tvSearchResult = view.findViewById<TextView>(R.id.tvSearchResult)
        val btnAddFriend = view.findViewById<Button>(R.id.btnAddFriend)
        val rvFriends = view.findViewById<RecyclerView>(R.id.rvFriends)

        rvFriends.layoutManager = LinearLayoutManager(requireContext())

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        var searchJob: kotlinx.coroutines.Job? = null

        etSearchFriend.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString()?.trim().orEmpty()

                if (query.length < 3) {
                    tvSearchResult.visibility = View.GONE
                    cardSearchResult.visibility = View.GONE
                    btnAddFriend.visibility = View.GONE
                    foundUser = null
                    searchJob?.cancel()
                    return
                }

                searchJob?.cancel()
                searchJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(500)
                    showLoading()
                    userViewModel.searchUser(query) { user ->
                        requireActivity().runOnUiThread {
                            hideLoading()
                            if (user == null || user.uid == currentUid) {
                                tvSearchResult.text = "User not found"
                                tvSearchResult.visibility = View.VISIBLE
                                cardSearchResult.visibility = View.VISIBLE
                                btnAddFriend.visibility = View.GONE
                                foundUser = null
                            } else {
                                foundUser = user
                                tvSearchResult.text = "Found: ${user.username}"
                                tvSearchResult.visibility = View.VISIBLE
                                cardSearchResult.visibility = View.VISIBLE
                                btnAddFriend.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })

        btnAddFriend.setOnClickListener {
            val friend = foundUser ?: return@setOnClickListener

            showLoading()
            userViewModel.addFriend(currentUid, friend.uid) { success ->
                requireActivity().runOnUiThread {
                    hideLoading()
                    if (success) {
                        btnAddFriend.visibility = View.GONE
                        cardSearchResult.visibility = View.GONE
                        etSearchFriend.setText("")
                        foundUser = null
                        loadFriends(currentUid, rvFriends)
                    } else {
                        Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val btnDone = view.findViewById<Button>(R.id.btnDone)
        btnDone.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        loadFriends(currentUid, rvFriends)
    }

    private fun loadFriends(currentUid: String, rvFriends: RecyclerView) {
        userViewModel.getFriends(currentUid) { friends ->
            requireActivity().runOnUiThread {
                rvFriends.adapter = FriendsAdapter(friends)
            }
        }
    }
}