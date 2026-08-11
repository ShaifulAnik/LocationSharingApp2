package com.example.locationsharingapp2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.locationsharingapp2.databinding.ActivityMyProfileBinding
import com.example.locationsharingapp2.model.AppUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadUserData()

        binding.btnUpdateName.setOnClickListener {
            val newName = binding.etProfileName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateProfile(newName)
            }
        }

        binding.btnShowOnMap.setOnClickListener {
            startActivity(Intent(this, GoogleMapActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("AppUsers").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(AppUser::class.java)
                if (user != null) {
                    binding.etProfileName.setText(user.displayName ?: "")
                    binding.tvProfileEmail.text = "Email: ${user.userEmail}"
                    binding.tvProfileLatitude.text = "Latitude: ${user.latitude}"
                    binding.tvProfileLongitude.text = "Longitude: ${user.longitude}"
                }
            }
    }

    private fun updateProfile(newName: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val updates = mutableMapOf<String, Any>(
            "displayName" to newName
        )

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    updates["latitude"] = location.latitude
                    updates["longitude"] = location.longitude
                }
                performFirestoreUpdate(currentUserId, updates)
            }.addOnFailureListener {
                performFirestoreUpdate(currentUserId, updates)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            performFirestoreUpdate(currentUserId, updates)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Update again to save location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performFirestoreUpdate(uid: String, updates: Map<String, Any>) {
        firestore.collection("AppUsers").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                val newName = updates["displayName"] as? String
                if (newName != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)
                }

                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                loadUserData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
