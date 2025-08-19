package com.example.myapplication2.data.model

data class SpfRoutine (
    val id: Int,
    val user_id: Int,
    var start_time: String,
    var end_time: String,
    var interval_minutes:Int,
    var active_days: String,
    var product_id:Int
)