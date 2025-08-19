package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Concerns
import com.example.myapplication2.data.model.Gendre
import com.example.myapplication2.data.model.UserDetails
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class IntroFlowRegister: AppCompatActivity() {
    private var selectedSex: String? = null
    private var encodedImage: String? = null
    private var selectedButton:Boolean?=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile_picture_page)

        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
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



        val continue_button=findViewById<Button>(R.id.button8)
        val femaleCard = findViewById<MaterialCardView>(R.id.femaleCard)
        val textfemale: TextView=findViewById<TextView>(R.id.textfemale)
        val imagefemale: ImageView=findViewById<ImageView>(R.id.imagefemale)
        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        val lightBlueColor = ContextCompat.getColor(this, R.color.light_pastel_blue)
        val blueColor= ContextCompat.getColor(this,R.color.pastel_blue)
        val color = ContextCompat.getColor(this, R.color.white)
        val color2 = ContextCompat.getColor(this, R.color.black)
        val maleCard = findViewById<MaterialCardView>(R.id.maleCard)
        val textmale: TextView=findViewById<TextView>(R.id.textmale)
        val imagemale: ImageView=findViewById<ImageView>(R.id.imagemale)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val gendre=sharedPreferences.getString("sex",null)
        if (gendre == "Female" && source == "user") {
            femaleCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textfemale.setTextColor(color)
            imagefemale.setImageResource(R.drawable.female_white)

            maleCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textmale.setTextColor(color2)
            imagemale.setImageResource(R.drawable.male)
            continue_button.setBackgroundColor(darkBlueColor)

            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fuser)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageBytes = stream.toByteArray()
                val encoded = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                withContext(Dispatchers.Main) {
                    encodedImage = encoded
                    selectedSex = "Female"
                    selectedButton = true
                }
            }
        }

        if (gendre == "Male" && source == "user") {
            maleCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textmale.setTextColor(color)
            imagemale.setImageResource(R.drawable.male_white)
            continue_button.setBackgroundColor(darkBlueColor)

            femaleCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textfemale.setTextColor(color2)
            imagefemale.setImageResource(R.drawable.female)

            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.muser)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageBytes = stream.toByteArray()
                val encoded = Base64.encodeToString(imageBytes, Base64.DEFAULT)

                withContext(Dispatchers.Main) {
                    encodedImage = encoded
                    selectedSex = "Male"
                    selectedButton = true
                }
            }
        }


        femaleCard.setOnClickListener {

            femaleCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textfemale.setTextColor(color)
            imagefemale.setImageResource(R.drawable.female_white)

            maleCard.backgroundTintList= ColorStateList.valueOf(lightBlueColor)
            textmale.setTextColor(color2)
            imagemale.setImageResource(R.drawable.male)
            continue_button.setBackgroundColor(darkBlueColor)

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fuser)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageBytes = stream.toByteArray()
             encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            selectedSex="Female"
            selectedButton=true
        }


        maleCard.setOnClickListener {
            maleCard.backgroundTintList= ColorStateList.valueOf(blueColor)

            textmale.setTextColor(color)
            imagemale.setImageResource(R.drawable.male_white)
            continue_button.setBackgroundColor(darkBlueColor)


            femaleCard.backgroundTintList= ColorStateList.valueOf(lightBlueColor)
            textfemale.setTextColor(color2)
            imagefemale.setImageResource(R.drawable.female)

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.muser)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageBytes = stream.toByteArray()
            encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            selectedSex="Male"
            selectedButton=true

        }

        continue_button.setOnClickListener {

            if (source == "register") {
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    if (selectedSex != null) {
                        editor.putString("sex", selectedSex)
                    }

                    if (encodedImage != null) {
                        editor.putString("poza_profil", encodedImage)
                    }

                    editor.apply()


                    val intent = Intent(this, AgeFlowRegister::class.java)
                    intent.putExtra("flag", "register")
                    val options = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    startActivity(intent, options.toBundle())

                } else {
                    Toast.makeText(
                        this,
                        "Please select your gender to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else
            {
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    if (selectedSex != null) {
                        editor.putString("sex", selectedSex.toString())
                    }
                    if (encodedImage != null) {
                        editor.putString("poza_profil", encodedImage.toString())
                    }
                    editor.apply()


                    val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val concc= Gendre(aux,selectedSex.toString(),encodedImage.toString())
                    val token = sharedPreferences.getString("session_token", null)
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                    } else {
                    val api = RetrofitInstance.instance
                    api.changeGendre("Bearer $token",concc).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
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
                } else {
                    Toast.makeText(
                        this,
                        "Please select your gender to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
}