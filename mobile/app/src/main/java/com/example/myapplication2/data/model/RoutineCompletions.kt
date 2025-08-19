package com.example.myapplication2.data.model

data class DailyLog(
    val id: Int,
    val user_id: Int,
    val log_date: String,
    val skin_feeling_score: Int?,
    val skin_condition: String?,
    val notes: String?,
    val weather: String?,
    val stress_level: Int?
)

data class RoutineCompletion(
    val id: Int,
    val user_id: Int,
    var completion_date: String,
    var routine_id: Int?,
    var steps: MutableList<Int>,
    var spf_id: Int?
)