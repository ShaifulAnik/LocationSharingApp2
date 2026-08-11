package com.example.locationsharingapp2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.locationsharingapp2.databinding.ActivitySignUpBinding
import com.example.locationsharingapp2.model.AppUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        fetchLocationAndSaveUser(name)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGoToSignIn.setOnClickListener {
            finish()
        }
    }

    private fun fetchLocationAndSaveUser(displayName: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            // Save with 0.0 for now, can be updated later
            saveUserToFirestore(displayName, 0.0, 0.0)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val lat = location?.latitude ?: 0.0
            val lng = location?.longitude ?: 0.0
            saveUserToFirestore(displayName, lat, lng)
        }.addOnFailureListener {
            saveUserToFirestore(displayName, 0.0, 0.0)
        }
    }

    private fun saveUserToFirestore(name: String, lat: Double, lng: Double) {
        val currentUser = auth.currentUser ?: return
        val appUser = AppUser(
            userId = currentUser.uid,
            userEmail = currentUser.email ?: "",
            displayName = name,
            latitude = lat,
            longitude = lng
        )

        firestore.collection("AppUsers")
            .document(currentUser.uid)
            .set(appUser)
            .addOnSuccessListener {
                navigateToFriendList()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                navigateToFriendList()
            }
    }

    private fun navigateToFriendList() {
        val intent = Intent(this, FriendListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Re-fetch location if permission granted
            val name = binding.etName.text.toString().trim()
            fetchLocationAndSaveUser(name)
        }
    }
}
