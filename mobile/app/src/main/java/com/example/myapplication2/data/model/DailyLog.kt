package com.example.myapplication2.data.model

data class DailyLogClass (
    val id: Int,
    val user_id: Int,
    val contents: List<DailyLogContent> = emptyList()
)

data class DailyLogContent (
    val id: Int,
    val daily_log_id: Int,
    val skin_feeling_score: Int?,
    val skin_condition: String = "normal",
    val notes: String?,
    val weather: String?,
    val stress_level: Int?,
    val log_date: String
)