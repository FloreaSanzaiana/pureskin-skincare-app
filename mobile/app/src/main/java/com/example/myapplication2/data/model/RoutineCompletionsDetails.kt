package com.example.myapplication2.data.model

data class RoutineCompletionDetails(
    val id: Int,
    var completion_date: String,
    val user_id: Int,
    var routine_type: String,
    var steps: MutableList<String>,
    var max_steps: Int
)