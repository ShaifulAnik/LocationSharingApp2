package com.example.locationsharingapp2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.locationsharingapp2.databinding.ItemUserBinding
import com.example.locationsharingapp2.model.AppUser

class UserAdapter(
    private val userList: List<AppUser>,
    private val onItemClick: (AppUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.binding.tvName.text = user.displayName ?: "No Name Provided"
        holder.binding.tvEmail.text = user.userEmail
        holder.binding.tvLat.text = "Lat: ${user.latitude}"
        holder.binding.tvLng.text = "Lng: ${user.longitude}"

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = userList.size
}