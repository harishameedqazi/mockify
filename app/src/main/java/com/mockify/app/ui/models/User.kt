package com.mockify.app.ui.models

data class User(
    var id: String = "",           // User ID
    val email: String = "",        // Email
    val deviceId: String = "",     // Device ID
    val loginDate: String = "",    // Login Date
    val loginTime: Long = 0L,      // Epoch time in milliseconds
    val validDays: Int = 0         // Validity period in days
)
