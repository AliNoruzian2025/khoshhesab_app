package com.example.khoshhesab_app.data.model

data class ExpenseSplit(
    val id: Int,
    val expense_id: Int,
    val user_id: Int,
    val share_amount: String,
    val user: User? = null
)