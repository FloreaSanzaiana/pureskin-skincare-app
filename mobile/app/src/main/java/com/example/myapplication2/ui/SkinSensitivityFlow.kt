package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.SkinPhototype
import com.example.myapplication2.data.model.SkinSensitivity
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SkinSensitivityFlow: AppCompatActivity() {
    private var selectedButton:Boolean?=false
    private var skintype: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.skin_sensitivity_page)
        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, SkinPhotoTypeFlow::class.java)
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
        val blueColor = ContextCompat.getColor(this, R.color.pastel_blue)
        val color = ContextCompat.getColor(this, R.color.white)
        val color2 = ContextCompat.getColor(this, R.color.black)
        val grey = ContextCompat.getColor(this, R.color.grey)
        val lightgrey = ContextCompat.getColor(this, R.color.light_grey)
        val continue_button = findViewById<Button>(R.id.button8)

        val notcard:MaterialCardView = findViewById<MaterialCardView>(R.id.notCard)
        val somewhatcard:MaterialCardView = findViewById<MaterialCardView>(R.id.somewhatCard)
        val verycard:MaterialCardView = findViewById<MaterialCardView>(R.id.veryCard)

        val descnot: TextView=findViewById<TextView>(R.id.descriptionnot)
        val descsomewhat: TextView=findViewById<TextView>(R.id.descriptionsomewhat)
        val descvery: TextView=findViewById<TextView>(R.id.descriptionvery)

        val textnot: TextView=findViewById<TextView>(R.id.textnot)
        val textsomewhat: TextView=findViewById<TextView>(R.id.textsomewhat)
        val textvery: TextView=findViewById<TextView>(R.id.textvery)

        val imagenot: ImageView=findViewById<ImageView>(R.id.imagenot)
        val imagesomewhat: ImageView=findViewById<ImageView>(R.id.imagesomewhat)
        val imagevery: ImageView=findViewById<ImageView>(R.id.imagevery)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val sensitivty=sharedPreferences.getString("skin_sensitivity", "")


        if(source=="user")
        {
            if(sensitivty=="Not sensitive at all"){
                notcard.backgroundTintList = ColorStateList.valueOf(blueColor)
                textnot.setTextColor(color)
                descnot.setTextColor(lightgrey)
                imagenot.setImageResource(R.drawable.smiley_white)


                somewhatcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textsomewhat.setTextColor(color2)
                descsomewhat.setTextColor(grey)
                imagesomewhat.setImageResource(R.drawable.neutral)

                verycard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textvery.setTextColor(color2)
                descvery.setTextColor(grey)
                imagevery.setImageResource(R.drawable.sad)

                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)
                skintype.clear()
                skintype.add("Not sensitive at all")
            }
            else if(sensitivty=="Somewhat sensitive")
            {
                notcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textnot.setTextColor(color2)
                descnot.setTextColor(grey)
                imagenot.setImageResource(R.drawable.smiley)


                somewhatcard.backgroundTintList = ColorStateList.valueOf(blueColor)
                textsomewhat.setTextColor(color)
                descsomewhat.setTextColor(lightgrey)
                imagesomewhat.setImageResource(R.drawable.neutrat_white)

                verycard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textvery.setTextColor(color2)
                descvery.setTextColor(grey)
                imagevery.setImageResource(R.drawable.sad)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)

                skintype.clear()
                skintype.add("Somewhat sensitive")
            }
            else{
                notcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textnot.setTextColor(color2)
                descnot.setTextColor(grey)
                imagenot.setImageResource(R.drawable.smiley)


                somewhatcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                textsomewhat.setTextColor(color2)
                descsomewhat.setTextColor(grey)
                imagesomewhat.setImageResource(R.drawable.neutral)

                verycard.backgroundTintList = ColorStateList.valueOf(blueColor)
                textvery.setTextColor(color)
                descvery.setTextColor(lightgrey)
                imagevery.setImageResource(R.drawable.sad_white)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)
                skintype.clear()
                skintype.add("Very sensitive")
            }
        }

        notcard.setOnClickListener {
            notcard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textnot.setTextColor(color)
            descnot.setTextColor(lightgrey)
            imagenot.setImageResource(R.drawable.smiley_white)


            somewhatcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textsomewhat.setTextColor(color2)
            descsomewhat.setTextColor(grey)
            imagesomewhat.setImageResource(R.drawable.neutral)

            verycard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textvery.setTextColor(color2)
            descvery.setTextColor(grey)
            imagevery.setImageResource(R.drawable.sad)

            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)
            skintype.clear()
            skintype.add("Not sensitive at all")
        }

        verycard.setOnClickListener {
            notcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textnot.setTextColor(color2)
            descnot.setTextColor(grey)
            imagenot.setImageResource(R.drawable.smiley)


            somewhatcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textsomewhat.setTextColor(color2)
            descsomewhat.setTextColor(grey)
            imagesomewhat.setImageResource(R.drawable.neutral)

            verycard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textvery.setTextColor(color)
            descvery.setTextColor(lightgrey)
            imagevery.setImageResource(R.drawable.sad_white)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)
            skintype.clear()
            skintype.add("Very sensitive")
        }

        somewhatcard.setOnClickListener {
            notcard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textnot.setTextColor(color2)
            descnot.setTextColor(grey)
            imagenot.setImageResource(R.drawable.smiley)


            somewhatcard.backgroundTintList = ColorStateList.valueOf(blueColor)
            textsomewhat.setTextColor(color)
            descsomewhat.setTextColor(lightgrey)
            imagesomewhat.setImageResource(R.drawable.neutrat_white)

            verycard.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            textvery.setTextColor(color2)
            descvery.setTextColor(grey)
            imagevery.setImageResource(R.drawable.sad)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)

            skintype.clear()
            skintype.add("Somewhat sensitive")
        }


        continue_button.setOnClickListener {
            if (source == "register") {

                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("skin_sensitivity", skintype.joinToString(","))
                    editor.apply()

                    val savedString = sharedPreferences.getString("skin_sensitivity", null)


                    val intent = Intent(this, ConcernsFlow::class.java)
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
                        "Please select your skin sensitivity to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("skin_sensitivity", skintype.joinToString(","))
                    editor.apply()





                    val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val concc= SkinSensitivity(aux,skintype)
                    val token = sharedPreferences.getString("session_token", null)
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                    } else {
                    val api = RetrofitInstance.instance
                    api.changeSkinSensitivity("Bearer $token",concc).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
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
                        "Please select your skin sensitivity to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}