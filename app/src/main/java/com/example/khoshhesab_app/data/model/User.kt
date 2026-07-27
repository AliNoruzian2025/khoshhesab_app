package com.example.khoshhesab_app.data.model

data class User(
    val id: Int,
    val name: String,
    val phone: String,
    val avatar: String? = null
)

data class LoginResponse(
    val message: String,
    val user: User,
    val token: String
)