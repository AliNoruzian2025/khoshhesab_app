package com.example.khoshhesab_app.data.model

data class Group(
    val id: Int,
    val name: String,
    val type: String, // "dang" یا "account"
    val avatar: String? = null,
    val creator_id: Int,
    val invite_code: String? = null,
    val require_approval: Boolean = true,
    val due_date: String? = null,
    val reminder_date: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val is_active: Boolean = true,
    val parent_group_id: Int? = null
)