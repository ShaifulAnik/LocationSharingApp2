package com.example.locationsharingapp2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.locationsharingapp2.adapters.UserAdapter
import com.example.locationsharingapp2.databinding.ActivityFriendListBinding
import com.example.locationsharingapp2.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.rvFriends.layoutManager = LinearLayoutManager(this)

        // Setup Drawer click
        binding.ivMenu.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // Setup Drawer Navigation
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, MyProfileActivity::class.java))
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, GoogleMapActivity::class.java))
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Handle Back Press to close drawer
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // View All on Map Button
        binding.btnViewAllOnMap.setOnClickListener {
            startActivity(Intent(this, GoogleMapActivity::class.java))
        }

        updateDrawerHeader()
    }

    private fun updateDrawerHeader() {
        try {
            val headerView = binding.navigationView.getHeaderView(0)
            val tvName = headerView.findViewById<TextView>(R.id.tvNavHeaderName)
            val tvEmail = headerView.findViewById<TextView>(R.id.tvNavHeaderEmail)

            val currentUser = auth.currentUser
            if (currentUser != null) {
                tvEmail.text = currentUser.email
                
                firestore.collection("AppUsers").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(AppUser::class.java)
                        tvName.text = user?.displayName ?: "User Name"
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
        // View All on Map Button
        binding.btnViewAllOnMap.setOnClickListener {
            startActivity(Intent(this, GoogleMapActivity::class.java))
        }

        updateDrawerHeader()
    }

    private fun loadUsers() {
        firestore.collection("AppUsers")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = mutableListOf<AppUser>()
                val currentUserId = auth.currentUser?.uid
                
                for (doc in querySnapshot.documents) {
                    val user = doc.toObject(AppUser::class.java)
                    if (user != null && user.userId != currentUserId) {
                        userList.add(user)
                    }
                }
                
                binding.rvFriends.adapter = UserAdapter(userList) { user ->
                    val intent = Intent(this, GoogleMapActivity::class.java)
                    intent.putExtra("selectedLatitude", user.latitude)
                    intent.putExtra("selectedLongitude", user.longitude)
                    intent.putExtra("selectedUserName", user.displayName ?: user.userEmail)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
