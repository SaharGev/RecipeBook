package com.example.recipebook.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipebook.R
import com.example.recipebook.db.UserEntity

class FriendsAdapter(
    private val friends: MutableList<UserEntity>,
    private val onRemoveFriend: (UserEntity) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvFriendEmail: TextView = view.findViewById(R.id.tvFriendEmail)
        val btnRemoveFriend: ImageButton = view.findViewById(R.id.btnRemoveFriend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.tvFriendName.text = friend.username
        holder.tvFriendEmail.text = friend.email
        holder.btnRemoveFriend.setOnClickListener {
            onRemoveFriend(friend)
        }
    }

    override fun getItemCount() = friends.size
}