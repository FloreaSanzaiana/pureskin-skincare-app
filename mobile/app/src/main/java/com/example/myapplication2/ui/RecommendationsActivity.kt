package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.RecommendationCard
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecommendationsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendations_page)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_recommendations

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    val intent = Intent(this, MainMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_products -> {
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_recommendations -> {
                    true
                }
                R.id.nav_diary -> {
                    val intent = Intent(this, DiaryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)

                    true
                }
                else -> false
            }

        }



        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val recommendationList = listOf(
            RecommendationCard("Recommend a product","AI-powered product matching" ,R.drawable.one_product),
            RecommendationCard("Recommend a routine","Full skincare routine personalised" ,R.drawable.multiple_products)
        )

        val adapter = RecommendationAdapter(recommendationList) { recommendation ->

            if(recommendation.title=="Recommend a product")
            {
                val intent = Intent(this, ProductTypeRecommend::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }
            else {
                val intent = Intent(this, RoutineTypeRecommendation::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }
        }

        recyclerView.adapter = adapter
    }
}