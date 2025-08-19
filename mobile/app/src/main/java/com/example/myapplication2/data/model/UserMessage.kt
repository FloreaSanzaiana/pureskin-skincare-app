package com.example.myapplication2.data.model

data class UserMessage (
    val message_id: Int,
    val user_id: Int,
    var sender: String,
    var text: String,
    var timestamp: String
)