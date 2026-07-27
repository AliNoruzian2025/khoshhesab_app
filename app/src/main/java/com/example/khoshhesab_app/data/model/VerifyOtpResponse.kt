package com.example.khoshhesab_app.data.model

data class VerifyOtpResponse(
    val message: String,
    val exists: Boolean,
    val completed: Boolean
)