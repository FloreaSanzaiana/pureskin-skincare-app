package com.example.myapplication2.data.model

data class UserRegisterDetails(
    val email: String,
    val username: String,
    val password_hash:String,
    val varsta: Int,
    val sex: String,
    val skin_type: List<String>,
    val skin_sensitivity: List<String>,
    val skin_phototype: List<String>,
    val concerns: List<String>,
    val poza_profil: String
)
