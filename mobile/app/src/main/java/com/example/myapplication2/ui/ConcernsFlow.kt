package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Concerns
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConcernsFlow : AppCompatActivity() {
    private lateinit var concernContainer: FlexboxLayout
    private lateinit var continueButton: Button
    private val selectedConcerns = mutableSetOf<String>()
    private var selectedButton:Boolean?=false
    private val concerns = listOf(
        "Acne & Blemishes", "Anti-Aging", "Black Heads", "Dark Circles", "Dark Spots",
        "Dryness", "Dullness", "Fine Lines & Wrinkles", "Loss of Firmness", "Oiliness",
        "Puffiness", "Redness", "Uneven Texture", "Visible Pores"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.concerns_page)

        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, SkinSensitivityFlow::class.java)
                intent.putExtra("flag", "register")
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right

                )
                startActivity(intent, options.toBundle())
            }
        }
        else{
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, UserDetailsActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left

                )
                startActivity(intent, options.toBundle())
            }
        }



        val lightBlueColor = ContextCompat.getColor(this, R.color.light_pastel_blue)
        val blueColor= ContextCompat.getColor(this,R.color.pastel_blue)
        val darkPurple = Color.parseColor("#6200EE")  // Mov închis
        val lightPastelPurple = Color.parseColor("#D1C4E9")
        // UI references
        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        val darkBlue = ContextCompat.getColor(this, R.color.button_disabled)

        concernContainer = findViewById(R.id.concernContainer)
        continueButton = findViewById(R.id.button8)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val concernsString = sharedPreferences.getString("concerns", "")
        val concernsList = concernsString?.split(",")?.toList() ?: emptyList()

        concerns.forEach { concern ->
            val button = MaterialButton(this).apply {
                text = concern
                isCheckable = true
                isClickable = true
                isFocusable = true
                setBackgroundColor(lightPastelPurple)
                setTextColor(ContextCompat.getColorStateList(context, R.color.toggle_text_selector))
                setTextSize(16f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }

                if(concern in concernsList && source == "user"){
                    selectedButton=true
                    continueButton.setBackgroundColor(darkBlueColor)
                    setBackgroundColor(darkPurple)
                    selectedConcerns.add(concern)
                    isChecked=true
                    updateContinueButton()
                }

                setOnClickListener {

                    if (isChecked) {
                        selectedButton=true
                        continueButton.setBackgroundColor(darkBlueColor)
                        setBackgroundColor(darkPurple)
                        selectedConcerns.add(concern)
                    } else {
                        setBackgroundColor(lightPastelPurple)
                        selectedConcerns.remove(concern)
                    }
                    updateContinueButton()
                }
            }
            concernContainer.addView(button)
        }
        continueButton.setOnClickListener {

            if (source == "register") {
                if (selectedButton == false) {
                    Toast.makeText(
                        this,
                        "Please select your concerns to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val sharedPreferences =
                        getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    val concernsString = selectedConcerns.joinToString(",")
                    editor.putString("concerns", concernsString)

                    editor.apply()
                    val savedString = sharedPreferences.getString("concerns", null)

                    val intent = Intent(this, CommitRegister::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    startActivity(intent, options.toBundle())

                }
            }else{
                if (selectedButton == false) {
                    Toast.makeText(
                        this,
                        "Please select your concerns to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val concernsString = selectedConcerns.joinToString(",")
                    editor.putString("concerns", concernsString)
                    editor.apply()




                    val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val concc= Concerns(aux,selectedConcerns.toList())
                    val token = sharedPreferences.getString("session_token", null)
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                    } else {
                    val api = RetrofitInstance.instance
                    api.changeConcerns("Bearer $token",concc).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                        override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                            when (response.code()) {
                                200 -> { }
                            }
                        }
                        override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {

                            Log.e("Error", t.message ?: "Unknown error")
                        }

                    })}





                    val intent = Intent(this, UserDetailsActivity::class.java)
                    intent.putExtra("flag", "user")
                    val options = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    startActivity(intent, options.toBundle())

                    finish()
                }
            }
        }
    }

    private fun updateContinueButton() {
        if(selectedConcerns.size==0){
            selectedButton=false
            val darkBlue = ContextCompat.getColor(this, R.color.button_disabled)
            continueButton.setBackgroundColor(darkBlue)
        }
        continueButton.text = "Continue (${selectedConcerns.size} concerns)"

    }

}
