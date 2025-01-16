package com.example.timecatcher.data.model

data class UserProfile(
    val fullName: String,
    val username: String,
    val description: String,
    val email: String,
    // Podrías agregar un remoteId si el backend devuelve un _id
    val remoteId: String? = null
)