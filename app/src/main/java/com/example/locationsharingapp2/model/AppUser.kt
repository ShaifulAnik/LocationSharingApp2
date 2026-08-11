package com.example.locationsharingapp2.model

data class AppUser(
    val userId: String = "",
    val userEmail: String = "",
    val displayName: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)