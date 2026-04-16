package com.example.recipebook.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.viewmodel.CommentViewModel
import com.example.recipebook.viewmodel.UserViewModel
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth

class CommentsBottomSheetFragment : BottomSheetDialogFragment() {

    private val commentViewModel: CommentViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyComments: TextView

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return inflater.inflate(R.layout.fragment_comments_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvComments)
        tvEmptyComments = view.findViewById(R.id.tvEmptyComments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val recipeId = arguments?.getInt("recipeId") ?: return

        val etComment = view.findViewById<EditText>(R.id.etComment)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        btnSend.setOnClickListener {
            val text = etComment.text.toString()
            if (text.isBlank()) return@setOnClickListener

            val currentUser = FirebaseAuth.getInstance().currentUser

            commentViewModel.addComment(
                recipeId = recipeId,
                userUid = currentUser?.uid ?: "",
                username = currentUser?.displayName ?: "Anonymous",
                text = text
            ) {
                etComment.text.clear()
                loadComments(recipeId) // refresh
            }
        }

        loadComments(recipeId)
    }

    private fun loadComments(recipeId: Int) {
        commentViewModel.getComments(recipeId) { list ->

            if (list.isEmpty()) {
                tvEmptyComments.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmptyComments.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val uiList = list.map {
                    Comment(
                        username = it.username,
                        text = it.text
                    )
                }

                recyclerView.adapter = CommentsAdapter(uiList)
            }
        }
    }
}