package com.example.khoshhesab_app.data.model

data class Settlement(
    val id: Int,
    val group_id: Int,
    val debtor_id: Int,
    val creditor_id: Int,
    val amount: String,
    val receipt_image: String? = null,
    val status: String, // pending, approved, rejected
    val rejection_reason: String? = null,
    val settled_at: String? = null,
    val debtor: User? = null,
    val creditor: User? = null
)