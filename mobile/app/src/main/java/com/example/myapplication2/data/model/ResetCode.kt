package com.example.myapplication2.data.model

import java.sql.Date

data class ResetCode(
    val email: String,
    val password_hash: String,
    val reset_token: String,
    val token_expires: Date
)