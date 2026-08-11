package com.example.locationsharingapp2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.locationsharingapp2.databinding.ActivityGoogleMapBinding
import com.example.locationsharingapp2.model.AppUser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGoogleMapBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val selectedLat = intent.getDoubleExtra("selectedLatitude", 0.0)
        val selectedLng = intent.getDoubleExtra("selectedLongitude", 0.0)
        val currentUserId = auth.currentUser?.uid

        firestore.collection("AppUsers").get()
            .addOnSuccessListener { querySnapshot ->
                var currentUserLatLng: LatLng? = null

                for (doc in querySnapshot.documents) {
                    val user = doc.toObject(AppUser::class.java)
                    if (user != null && user.latitude != 0.0 && user.longitude != 0.0) {
                        val userLatLng = LatLng(user.latitude, user.longitude)
                        
                        // Check if this is the current user
                        val isCurrentUser = user.userId == currentUserId
                        val titleName = if (isCurrentUser) "Me (${user.displayName ?: user.userEmail})" else (user.displayName ?: user.userEmail)
                        
                        // Set marker color: Blue for current user, Red for others
                        val markerIcon = if (isCurrentUser) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        }

                        googleMap.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title(titleName)
                                .icon(markerIcon)
                        )

                        if (isCurrentUser) {
                            currentUserLatLng = userLatLng
                        }
                    }
                }

                // Zoom logic
                if (selectedLat != 0.0 && selectedLng != 0.0) {
                    val selectedLatLng = LatLng(selectedLat, selectedLng)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f))
                } else if (currentUserLatLng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLng, 12f))
                }
            }
    }
}