package com.example.myapplication2.data.model

data class ProductRecommended(
    val id: Int,
    val product_name: String,
    val product_type: String,
    val clean_ingreds: String,
    val area: String,
    val time: String,
    val spf: Int,
    val product_url: String,
    val price: String,
    val irritating_ingredients: String
)
