package com.example.myapplication2.model

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()-1,
    val isTyping: Boolean = false
) {
    fun getFormattedTime(): String {

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val result = sdf.format(Date(timestamp))


        return result
    }

}