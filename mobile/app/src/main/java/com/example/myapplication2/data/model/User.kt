package com.example.myapplication2.data.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val password_hash: String,
    val reset_token: String,
    val token_expires: String
)