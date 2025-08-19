package com.example.myapplication2.data.model

data class Routine (
    val user_id: Int,
    val routine_type: String,
    var notify_time: String,
    var notify_days: String,
    val routine_id: Int
)