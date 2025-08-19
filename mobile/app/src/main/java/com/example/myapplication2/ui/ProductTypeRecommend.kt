package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.SkinType
import com.example.myapplication2.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductTypeRecommend: AppCompatActivity() {
    private var selectedButton:Boolean?=false
    private var selectedtype: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_type_recommend)

        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)
        val continue_button=findViewById<Button>(R.id.button8)
        button.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }

        val radioButtons = listOf(
            findViewById<RadioButton>(R.id.radioButtonMoisturiser),
            findViewById<RadioButton>(R.id.radioButtonSerum),
            findViewById<RadioButton>(R.id.radioButtonPeel),
            findViewById<RadioButton>(R.id.radioButtonEyeCare),
            findViewById<RadioButton>(R.id.radioButtonCleanser),
            findViewById<RadioButton>(R.id.radioButtonToner),
            findViewById<RadioButton>(R.id.radioButtonExfoliator),
            findViewById<RadioButton>(R.id.radioButtonSunscreen),
            findViewById<RadioButton>(R.id.radioButtonMakeupRemover),
            findViewById<RadioButton>(R.id.radioButtonSpray),
            findViewById<RadioButton>(R.id.radioButtonMask),
            findViewById<RadioButton>(R.id.radioButtonOil),
            findViewById<RadioButton>(R.id.radioeyemask),
            findViewById<RadioButton>(R.id.radiolipmask)
        )
        val darkBlueColor = ContextCompat.getColor(this, R.color.dark_blue)
        radioButtons.forEach { radioButton ->
            radioButton.setOnClickListener {
                radioButtons.filter { it != radioButton }.forEach { it.isChecked = false }
                selectedtype = radioButton.text.toString().lowercase()//.replace(" ", "")
                continue_button.setBackgroundColor(darkBlueColor)
                selectedButton=true


            }
        }

        continue_button.setOnClickListener {

            if (selectedButton == true) {
                val sharedPreferences =
                    getSharedPreferences("UserRecomm", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putString("product_type", selectedtype.toString())
                editor.apply()

                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("flag", "product")

                startActivity(intent)

                finishAffinity()
            } else {
                Toast.makeText(
                    this,
                    "Please select a product type to proceed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }
}