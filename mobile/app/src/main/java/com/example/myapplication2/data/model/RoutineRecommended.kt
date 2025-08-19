package com.example.myapplication2.data.model

data class Product_(
    val area: String,
    val clean_ingreds: String,
    val id: Int,
    val irritating_ingredients: String,
    val price: String,
    val product_name: String,
    val product_type: String,
    val product_url: String,
    val spf: Int,
    val time: String
)


data class RoutineRecommended(
    val product: Product_,
    val step: String
)


