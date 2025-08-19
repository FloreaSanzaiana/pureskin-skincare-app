package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.ProductRecommended
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProductRecommendationResult : AppCompatActivity() {
    private var selectedButton: Boolean? = false
    private lateinit var continue_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.product_result_page)

        val recyclerView: RecyclerView = findViewById(R.id.productRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        continue_button = findViewById(R.id.button8)

        val prefs = getSharedPreferences("UserRecomm", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("recommended_products", null)

        if (!jsonString.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<List<ProductRecommended>>() {}.type
            val productList: List<ProductRecommended> = gson.fromJson(jsonString, type)

            val adapter = ProductResultedAdapter(productList) { product ->
                openProductDetailsFragment(product)
            }
            recyclerView.adapter = adapter
        }

        continue_button.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun openProductDetailsFragment(product: ProductRecommended) {
        val new_product= Product(product.id,product.product_name,product.product_type,product.clean_ingreds,product.area,product.time,product.spf,product.product_url,product.price,product.irritating_ingredients)

        val fragment = ProductDetailsBottomSheetFragment(new_product)
        fragment.show(supportFragmentManager, fragment.tag)


        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        selectedButton = true
        continue_button.setBackgroundColor(darkBlueColor)
    }
}