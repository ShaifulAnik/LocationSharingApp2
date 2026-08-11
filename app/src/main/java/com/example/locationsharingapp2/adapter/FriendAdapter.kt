package com.example.locationsharingapp2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.locationsharingapp2.databinding.ItemUserBinding
import com.example.locationsharingapp2.model.AppUser

class FriendAdapter(private val userList: List<AppUser>) :
    RecyclerView.Adapter<FriendAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // Display Name না থাকলে Default Text "No Name Provided" দেখাবে
        holder.binding.tvName.text = user.displayName ?: "No Name Provided"
        holder.binding.tvEmail.text = user.userEmail
    }

    override fun getItemCount(): Int = userList.size
}