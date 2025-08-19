package com.example.myapplication2.data.model

data class UserRoutines (
    val id: Int,
    val routine_type: String,
    val user_id: Int,
    val notification_time: String,
    val notification_days: String,
    val steps: List<Step>
)

data class Step(
    val id: Int,
    val routine_id:Int,
    var step_order: Int,
    val step_name: String,
    val description: String,
    var product_id: Int?
)