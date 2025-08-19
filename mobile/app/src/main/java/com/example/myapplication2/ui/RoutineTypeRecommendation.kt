package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.google.android.material.card.MaterialCardView
import java.io.ByteArrayOutputStream


class RoutineTypeRecommendation: AppCompatActivity() {

    private var selectedButton:Boolean?=false
    private var routinetype: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routine_type_page)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)
        button.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }


        val continue_button=findViewById<Button>(R.id.button8)
        val femaleCard = findViewById<MaterialCardView>(R.id.morningCard)
        val textfemale: TextView=findViewById<TextView>(R.id.textfemale)
        val imagefemale: ImageView=findViewById<ImageView>(R.id.imagefemale)
        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        val lightBlueColor = ContextCompat.getColor(this, R.color.light_pastel_blue)
        val blueColor= ContextCompat.getColor(this,R.color.pastel_blue)
        val color = ContextCompat.getColor(this, R.color.white)
        val color2 = ContextCompat.getColor(this, R.color.black)
        val maleCard = findViewById<MaterialCardView>(R.id.eveningCard)
        val textmale: TextView=findViewById<TextView>(R.id.textmale)
        val imagemale: ImageView=findViewById<ImageView>(R.id.imagemale)


        femaleCard.setOnClickListener {

            femaleCard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textfemale.setTextColor(color)

            maleCard.backgroundTintList= ColorStateList.valueOf(lightBlueColor)
            textmale.setTextColor(color2)

            continue_button.setBackgroundColor(darkBlueColor)


            routinetype="morning"
            selectedButton=true
        }


        maleCard.setOnClickListener {
            maleCard.backgroundTintList= ColorStateList.valueOf(blueColor)

            textmale.setTextColor(color)
            continue_button.setBackgroundColor(darkBlueColor)


            femaleCard.backgroundTintList= ColorStateList.valueOf(lightBlueColor)
            textfemale.setTextColor(color2)


            routinetype="evening"
            selectedButton=true

        }

        continue_button.setOnClickListener {

                if (selectedButton == true) {

                    val sharedPreferences =
                        getSharedPreferences("UserRoutine", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    if (routinetype != null) {
                        editor.putString("routine_type", routinetype)
                    }


                    editor.apply()


                    val intent = Intent(this, LoadingActivity::class.java)
                    intent.putExtra("flag", "routine")
                    val options = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    startActivity(intent, options.toBundle())

                } else {
                    Toast.makeText(
                        this,
                        "Please select a routine type to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }}