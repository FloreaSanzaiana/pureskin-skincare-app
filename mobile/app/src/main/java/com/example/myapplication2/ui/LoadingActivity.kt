package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.ProductRecommended
import com.example.myapplication2.data.model.RoutineRecommended
import com.example.myapplication2.data.model.SkinType
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : AppCompatActivity() {
    private lateinit var searchingText: TextView
    private val searchingMessagess= listOf(
        "Analyzing your skin profile...",
        "Scanning product ingredients...",
        "Finding your perfect match...",
        "Evaluating ingredient compatibility...",
        "Creating personalized recommendations...",
        "Optimizing for your skin needs..."

    )

    private val searchingMessages = listOf(
        "Retinol loves the nighttime!",
        "Vitamin C is your glow bestie.",
        "Wow! Hyaluronic acid holds 1000x water.",
        "Niacinamide shrinks those pores!",
        "Amazing: Peptides are collagen boosters.",
        "Ceramides fix your skin barrier.",
        "Your skin drops 40,000 cells daily!",
        "Collagen party peaks at 25.",
        "Pro tip: Double cleanse = double glow.",
        "Stress = unwanted acne guests.",
        "Cold water = instant pore tightening.",
        "Sunscreen refresh every 2 hours!",
        "Sleep wrinkles are real things!",
        "Antioxidants are damage fighters.",
        "Green tea calms angry skin.",
        "Exfoliate twice weekly max!",
        "Face massage = circulation boost.",
        "Food shows up on your face.",
        "Moisturizer locks in the good stuff.",
        "UV sneaks through windows too!",
        "Don't forget your neck, babe.",
        "Hormones love causing acne drama.",
        "Beauty sleep repairs everything.",
        "Caffeine deflates puffy eyes.",
        "Oily skin still craves moisture.",
        "Face masks work in 15 minutes.",
        "pH balance keeps skin happy.",
        "Exercise pumps up circulation.",
        "Water temp matters for skin.",
        "Seasons shake up your routine.",
        "Genetics rule 60% of aging."
    ).shuffled().take(3)
    private  lateinit var  runnable: Runnable
    private var index=0
    private val handler= Handler(Looper.getMainLooper())
    private var apifinish=false
    private var messagefinish=false
    private lateinit var source: String
    private var area:String="face"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.loading_page)
        source = intent.getStringExtra("flag") ?:""


        searchingText=findViewById<TextView>(R.id.textsearching)

        runnable=object : Runnable{
            override fun run(){
                if (index < searchingMessages.size) {
                    searchingText.text = searchingMessages[index]
                    index++
                    handler.postDelayed(this, 2000)
                } else {
                    messagefinish = true
                    checktocontinue()
                }
            }

        }
        handler.post(runnable)

        if(source=="product"){
            val sp =getSharedPreferences("UserRecomm", Context.MODE_PRIVATE)
            var product_type=sp.getString("product_type",null)
            if(product_type=="eye care")
            {
                area="eye"
                product_type="Eye Care"
            }
            else if( product_type=="eye mask")
            {
                area="eye"
                product_type="mask"
            }
            else if(product_type=="lip mask")
            {
                area="lip"
                product_type="mask"
            }
            else if (product_type=="face mask")
            {
                area="face"
                product_type="mask"
            }
            Log.d("verificare","#"+product_type.toString()+"#"+area.toString()+"#")

            val sharedPreferences =
                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
            val token = sharedPreferences.getString("session_token", null)
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                apifinish=true

                checktocontinue()
            } else {
                val api = RetrofitInstance.instance
                api.getRecommendedProducts("Bearer $token",aux,product_type,area).enqueue(object : Callback<List<ProductRecommended>> {
                    override fun onResponse(call: Call<List<ProductRecommended>>, response: Response<List<ProductRecommended>>) {
                        when (response.code()) {
                            200 -> {

                                val products = response.body()!!
                                val gson = Gson()
                                val json = gson.toJson(products)

                                val sp =getSharedPreferences("UserRecomm", Context.MODE_PRIVATE)
                                val editor2=sp.edit()
                                editor2.putString("recommended_products", json)
                                editor2.apply()



                            }
                        }
                        apifinish=true

                        checktocontinue()
                    }
                    override fun onFailure(call: Call<List<ProductRecommended>>, t: Throwable) {
                        apifinish=true

                        checktocontinue()
                        Log.e("Error", t.message ?: "Unknown error")
                    }

                })}

        }else{
            Log.d("recomm","sunt pentru routine type")
            val sp =getSharedPreferences("UserRoutine", Context.MODE_PRIVATE)
            val product_type=sp.getString("routine_type",null)
            val editor2=sp.edit()
            val sharedPreferences =
                getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
            val token = sharedPreferences.getString("session_token", null)
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
            } else {
                val api = RetrofitInstance.instance
                api.getRecommendedRoutines("Bearer $token",aux,product_type).enqueue(object : Callback<List<RoutineRecommended>> {
                    override fun onResponse(call: Call<List<RoutineRecommended>>, response: Response<List<RoutineRecommended>>) {
                        when (response.code()) {
                            200 -> {

                                Log.d("recomm","succes")
                                val routines = response.body() ?: emptyList()

                                Log.d("recomm",routines.toString())

                                val gson = Gson()
                                val json = gson.toJson(routines)

                                val sp = getSharedPreferences("UserRoutine", Context.MODE_PRIVATE)
                                val editor2 = sp.edit()
                                editor2.putString("routine_response", json)
                                editor2.apply()
                                apifinish=true

                                checktocontinue()
                            }

                        }

                    }
                    override fun onFailure(call: Call<List<RoutineRecommended>>, t: Throwable) {
                        apifinish=true

                        checktocontinue()
                        Log.d("recomm","esec2")
                        Log.e("Error", t.message ?: "Unknown error")
                    }

                })}

            //apifinish=true

        }


    }
    override fun onDestroy(){
        super.onDestroy()
        handler.removeCallbacks (runnable )
    }

    private fun  checktocontinue(){
        if (apifinish && messagefinish) {
            handler.removeCallbacks(runnable)
            if(source=="product"){
                val intent = Intent(this, ProductRecommendationResult::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, RecommendedRoutineResult::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


}