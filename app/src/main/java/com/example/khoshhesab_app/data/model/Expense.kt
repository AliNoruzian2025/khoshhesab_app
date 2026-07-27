package com.example.khoshhesab_app.data.model

data class Expense(
    val id: Int,
    val group_id: Int,
    val payer_id: Int,
    val amount: String,
    val description: String,
    val receipt_image: String? = null,
    val expense_date: String,
    val payer: User? = null,
    val splits: List<ExpenseSplit>? = null
)