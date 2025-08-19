package com.example.myapplication2.data.model

data class SessionToken(
    val user_id: Int,
    val session_token: String,
    val expires_at: Long
)
