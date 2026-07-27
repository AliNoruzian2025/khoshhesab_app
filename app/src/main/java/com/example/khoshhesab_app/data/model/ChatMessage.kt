package com.example.khoshhesab_app.data.model

data class ChatMessage(
    val id: Int,
    val group_id: Int,
    val sender_id: Int,
    val message: String? = null,
    val reply_to_id: Int? = null,
    val sticker: String? = null,
    val receipt_image: String? = null,
    val is_seen: Boolean = false,
    val created_at: String,
    val sender: User? = null,
    val replyTo: ChatMessage? = null
)