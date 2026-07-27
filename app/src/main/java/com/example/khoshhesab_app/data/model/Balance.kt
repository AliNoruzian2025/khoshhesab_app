package com.example.khoshhesab_app.data.model

data class Balance(
    val user_id: Int,
    val name: String,
    val balance: String,
    val status: String // creditor, debtor, settled
)