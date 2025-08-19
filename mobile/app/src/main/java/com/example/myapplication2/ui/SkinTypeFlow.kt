package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.SkinSensitivity
import com.example.myapplication2.data.model.SkinType
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SkinTypeFlow: AppCompatActivity() {

    private var selectedButton:Boolean?=false
    private var skintype: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.skin_type)
        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, AgeFlowRegister::class.java)
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



        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        val lightBlueColor = ContextCompat.getColor(this, R.color.light_pastel_blue)
        val blueColor= ContextCompat.getColor(this,R.color.pastel_blue)
        val color = ContextCompat.getColor(this, R.color.white)
        val color2 = ContextCompat.getColor(this, R.color.black)

        val normalCard = findViewById<MaterialCardView>(R.id.normalCard)
        val normaltext=findViewById<TextView>(R.id.textnormal)
        val imagenormal=findViewById<ImageView>(R.id.imagenormal)

        val karmaCard=findViewById<MaterialCardView>(R.id.karmaCard)
        val karmatext=findViewById<TextView>(R.id.textkarma)
        val imagekarma=findViewById<ImageView>(R.id.imagekarma)


        val dryCard=findViewById<MaterialCardView>(R.id.dryCard)
        val drytext=findViewById<TextView>(R.id.textdry)
        val imagedry=findViewById<ImageView>(R.id.imagedry)

        val oilyCard=findViewById<MaterialCardView>(R.id.oilyCard)
        val oilytext=findViewById<TextView>(R.id.textoily)
        val imageoily=findViewById<ImageView>(R.id.imageoily)
        val continue_button=findViewById<Button>(R.id.button8)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val skin_type=sharedPreferences.getString("skin_type", "")
        if(source=="user") {
           if(skin_type=="Normal Skin")
           {
               normalCard.backgroundTintList = ColorStateList.valueOf(blueColor)
               normaltext.setTextColor(color)
               imagenormal.setImageResource(R.drawable.normal_white)

               karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               karmatext.setTextColor(color2)
               imagekarma.setImageResource(R.drawable.karma_skin)

               dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               drytext.setTextColor(color2)
               imagedry.setImageResource(R.drawable.dry_skin)

               oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               oilytext.setTextColor(color2)
               imageoily.setImageResource(R.drawable.oily_skin)
               continue_button.setBackgroundColor(darkBlueColor)
               selectedButton=true
               skintype.clear()
               skintype.add("Normal Skin")
           }
            else if(skin_type=="Karma Skin")
           {
               karmaCard.backgroundTintList = ColorStateList.valueOf(blueColor)
               karmatext.setTextColor(color)
               imagekarma.setImageResource(R.drawable.karma_white)

               normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               normaltext.setTextColor(color2)
               imagenormal.setImageResource(R.drawable.normal_skin)

               dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               drytext.setTextColor(color2)
               imagedry.setImageResource(R.drawable.dry_skin)

               oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               oilytext.setTextColor(color2)
               imageoily.setImageResource(R.drawable.oily_skin)
               continue_button.setBackgroundColor(darkBlueColor)
               selectedButton=true
               skintype.clear()
               skintype.add("Karma Skin")
            }
            else if(skin_type=="Dry Skin")
           {
               dryCard.backgroundTintList = ColorStateList.valueOf(blueColor)
               drytext.setTextColor(color)
               imagedry.setImageResource(R.drawable.dry_white)

               karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               karmatext.setTextColor(color2)
               imagekarma.setImageResource(R.drawable.karma_skin)
               normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               normaltext.setTextColor(color2)
               imagenormal.setImageResource(R.drawable.normal_skin)
               oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               oilytext.setTextColor(color2)
               imageoily.setImageResource(R.drawable.oily_skin)
               continue_button.setBackgroundColor(darkBlueColor)
               selectedButton=true
               skintype.clear()
               skintype.add("Dry Skin")
            }
            else{
               karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               karmatext.setTextColor(color2)
               imagekarma.setImageResource(R.drawable.karma_skin)
               normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               normaltext.setTextColor(color2)
               imagenormal.setImageResource(R.drawable.normal_skin)
               dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
               drytext.setTextColor(color2)
               imagedry.setImageResource(R.drawable.dry_skin)

               oilyCard.backgroundTintList = ColorStateList.valueOf(blueColor)
               oilytext.setTextColor(color)
               imageoily.setImageResource(R.drawable.oily_white)
               continue_button.setBackgroundColor(darkBlueColor)
               selectedButton=true
               skintype.clear()
               skintype.add("Oily Skin")
            }
        }

        normalCard.setOnClickListener {
           normalCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            normaltext.setTextColor(color)
            imagenormal.setImageResource(R.drawable.normal_white)

            karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            karmatext.setTextColor(color2)
            imagekarma.setImageResource(R.drawable.karma_skin)

            dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            drytext.setTextColor(color2)
            imagedry.setImageResource(R.drawable.dry_skin)

            oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            oilytext.setTextColor(color2)
            imageoily.setImageResource(R.drawable.oily_skin)
            continue_button.setBackgroundColor(darkBlueColor)
            selectedButton=true
            skintype.clear()
            skintype.add("Normal Skin")
        }

        karmaCard.setOnClickListener {
            karmaCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            karmatext.setTextColor(color)
            imagekarma.setImageResource(R.drawable.karma_white)

            normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            normaltext.setTextColor(color2)
            imagenormal.setImageResource(R.drawable.normal_skin)

            dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            drytext.setTextColor(color2)
            imagedry.setImageResource(R.drawable.dry_skin)

            oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            oilytext.setTextColor(color2)
            imageoily.setImageResource(R.drawable.oily_skin)
            continue_button.setBackgroundColor(darkBlueColor)
            selectedButton=true
            skintype.clear()
            skintype.add("Karma Skin")
        }

        dryCard.setOnClickListener {
            dryCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            drytext.setTextColor(color)
            imagedry.setImageResource(R.drawable.dry_white)

            karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            karmatext.setTextColor(color2)
            imagekarma.setImageResource(R.drawable.karma_skin)
            normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            normaltext.setTextColor(color2)
            imagenormal.setImageResource(R.drawable.normal_skin)
            oilyCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            oilytext.setTextColor(color2)
            imageoily.setImageResource(R.drawable.oily_skin)
            continue_button.setBackgroundColor(darkBlueColor)
            selectedButton=true
            skintype.clear()
            skintype.add("Dry Skin")
        }

        oilyCard.setOnClickListener {
            karmaCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            karmatext.setTextColor(color2)
            imagekarma.setImageResource(R.drawable.karma_skin)
            normalCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            normaltext.setTextColor(color2)
            imagenormal.setImageResource(R.drawable.normal_skin)
            dryCard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            drytext.setTextColor(color2)
            imagedry.setImageResource(R.drawable.dry_skin)

            oilyCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            oilytext.setTextColor(color)
            imageoily.setImageResource(R.drawable.oily_white)
            continue_button.setBackgroundColor(darkBlueColor)
            selectedButton=true
            skintype.clear()
            skintype.add("Oily Skin")
        }
        continue_button.setOnClickListener {
            if (source == "register") {

                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("skin_type", skintype.joinToString(","))
                    editor.apply()

                    val intent = Intent(this, SkinPhotoTypeFlow::class.java)
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
                        "Please select your skin type to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("skin_type", skintype.joinToString(","))
                    editor.apply()


                    val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val concc= SkinType(aux,skintype)
                    val token = sharedPreferences.getString("session_token", null)
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                    } else {
                    val api = RetrofitInstance.instance
                    api.changeSkinType("Bearer $token",concc).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
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
                        "Please select your skin type to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }
}