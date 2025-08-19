package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.Gendre
import com.example.myapplication2.data.model.SkinPhototype
import com.example.myapplication2.data.network.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SkinPhotoTypeFlow: AppCompatActivity() {
    private var selectedButton:Boolean?=false
    private var skintype: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.skin_phototype_page)

        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, SkinTypeFlow::class.java)
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

        val pale: MaterialCardView = findViewById<MaterialCardView>(R.id.palewhiteCard)
        val whiteskin: MaterialCardView = findViewById<MaterialCardView>(R.id.whiteCard)
        val lightbrown: MaterialCardView = findViewById<MaterialCardView>(R.id.lightbrownCard)
        val moderate: MaterialCardView = findViewById<MaterialCardView>(R.id.moderatebrownCard)
        val deep: MaterialCardView = findViewById<MaterialCardView>(R.id.deepbrownCard)
        val dark: MaterialCardView = findViewById<MaterialCardView>(R.id.darkbrownCard)

        val paletext: TextView = findViewById<TextView>(R.id.textpalewhite)
        val whitetext: TextView = findViewById<TextView>(R.id.textwhite)
        val moderatetext: TextView = findViewById<TextView>(R.id.textmoderatebrown)
        val lighttext: TextView = findViewById<TextView>(R.id.textlightbrown)
        val deeptext: TextView = findViewById<TextView>(R.id.textdeepbrown)
        val darktext: TextView = findViewById<TextView>(R.id.textdarkbrown)

        val paledesc: TextView = findViewById<TextView>(R.id.descriptionpale)
        val whitedesc: TextView = findViewById<TextView>(R.id.descriptionwhite)
        val moderatedesc: TextView = findViewById<TextView>(R.id.descriptionmoderatebrown)
        val lightdesc: TextView = findViewById<TextView>(R.id.descriptionlightbrown)
        val deepdesc: TextView = findViewById<TextView>(R.id.descriptiondeepbrown)
        val darkdesc: TextView = findViewById<TextView>(R.id.descriptiondarkbrown)


        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val phototype =sharedPreferences.getString("skin_phototype", "")

        if(source=="user")
        {
            if(phototype=="Pale white skin"){
                paledesc.setTextColor(lightgrey)
                whitedesc.setTextColor(grey)
                moderatedesc.setTextColor(grey)
                lightdesc.setTextColor(grey)
                deepdesc.setTextColor(grey)
                darkdesc.setTextColor(grey)
                pale.backgroundTintList = ColorStateList.valueOf(blueColor)
                paletext.setTextColor(color)

                whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                whitetext.setTextColor(color2)

                lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                lighttext.setTextColor(color2)

                moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                moderatetext.setTextColor(color2)

                deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                deeptext.setTextColor(color2)

                dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                darktext.setTextColor(color2)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)

                skintype.clear()
                skintype.add("Pale white skin")
            }
            else if(phototype=="White skin")
            {
                paledesc.setTextColor(grey)
                whitedesc.setTextColor(lightgrey)
                moderatedesc.setTextColor(grey)
                lightdesc.setTextColor(grey)
                deepdesc.setTextColor(grey)
                darkdesc.setTextColor(grey)
                pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                paletext.setTextColor(color2)

                whiteskin.backgroundTintList = ColorStateList.valueOf(blueColor)
                whitetext.setTextColor(color)

                lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                lighttext.setTextColor(color2)

                moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                moderatetext.setTextColor(color2)

                deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                deeptext.setTextColor(color2)

                dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                darktext.setTextColor(color2)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)
                skintype.clear()
                skintype.add("White skin")
            }
            else if(phototype=="Light brown skin")
            {
                paledesc.setTextColor(grey)
                whitedesc.setTextColor(grey)
                moderatedesc.setTextColor(grey)
                lightdesc.setTextColor(lightgrey)
                deepdesc.setTextColor(grey)
                darkdesc.setTextColor(grey)
                pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                paletext.setTextColor(color2)

                whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                whitetext.setTextColor(color2)

                lightbrown.backgroundTintList = ColorStateList.valueOf(blueColor)
                lighttext.setTextColor(color)

                moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                moderatetext.setTextColor(color2)

                deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                deeptext.setTextColor(color2)

                dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                darktext.setTextColor(color2)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)

                skintype.clear()
                skintype.add("Light brown skin")
            }
            else if(phototype=="Moderate brown skin")
            {
                paledesc.setTextColor(grey)
                whitedesc.setTextColor(grey)
                moderatedesc.setTextColor(lightgrey)
                lightdesc.setTextColor(grey)
                deepdesc.setTextColor(grey)
                darkdesc.setTextColor(grey)
                pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                paletext.setTextColor(color2)

                whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                whitetext.setTextColor(color2)

                lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                lighttext.setTextColor(color2)

                moderate.backgroundTintList = ColorStateList.valueOf(blueColor)
                moderatetext.setTextColor(color)

                deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                deeptext.setTextColor(color2)

                dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                darktext.setTextColor(color2)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)

                skintype.clear()
                skintype.add("Moderate brown skin")
            }
            else if(phototype=="Dark brown skin")
            {
                paledesc.setTextColor(grey)
                whitedesc.setTextColor(grey)
                moderatedesc.setTextColor(grey)
                lightdesc.setTextColor(grey)
                deepdesc.setTextColor(grey)
                darkdesc.setTextColor(lightgrey)
                pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                paletext.setTextColor(color2)

                whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                whitetext.setTextColor(color2)

                lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                lighttext.setTextColor(color2)

                moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                moderatetext.setTextColor(color2)

                deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                deeptext.setTextColor(color2)

                dark.backgroundTintList = ColorStateList.valueOf(blueColor)
                darktext.setTextColor(color)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)
                skintype.clear()
                skintype.add("Dark brown skin")
            }
            else if(phototype=="Deep brown skin")
            {
                paledesc.setTextColor(grey)
                whitedesc.setTextColor(grey)
                moderatedesc.setTextColor(grey)
                lightdesc.setTextColor(grey)
                deepdesc.setTextColor(lightgrey)
                darkdesc.setTextColor(grey)
                pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                paletext.setTextColor(color2)

                whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                whitetext.setTextColor(color2)

                lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                lighttext.setTextColor(color2)

                moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                moderatetext.setTextColor(color2)

                deep.backgroundTintList = ColorStateList.valueOf(blueColor)
                deeptext.setTextColor(color)

                dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
                darktext.setTextColor(color2)
                selectedButton=true
                continue_button.setBackgroundColor(darkBlueColor)

                skintype.clear()
                skintype.add("Deep brown skin")
            }
        }



        pale.setOnClickListener {
            paledesc.setTextColor(lightgrey)
            whitedesc.setTextColor(grey)
            moderatedesc.setTextColor(grey)
            lightdesc.setTextColor(grey)
            deepdesc.setTextColor(grey)
            darkdesc.setTextColor(grey)
            pale.backgroundTintList = ColorStateList.valueOf(blueColor)
            paletext.setTextColor(color)

            whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            whitetext.setTextColor(color2)

            lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            lighttext.setTextColor(color2)

            moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            moderatetext.setTextColor(color2)

            deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            deeptext.setTextColor(color2)

            dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            darktext.setTextColor(color2)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)

            skintype.clear()
            skintype.add("Pale white skin")
        }

        whiteskin.setOnClickListener {
            paledesc.setTextColor(grey)
            whitedesc.setTextColor(lightgrey)
            moderatedesc.setTextColor(grey)
            lightdesc.setTextColor(grey)
            deepdesc.setTextColor(grey)
            darkdesc.setTextColor(grey)
            pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            paletext.setTextColor(color2)

            whiteskin.backgroundTintList = ColorStateList.valueOf(blueColor)
            whitetext.setTextColor(color)

            lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            lighttext.setTextColor(color2)

            moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            moderatetext.setTextColor(color2)

            deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            deeptext.setTextColor(color2)

            dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            darktext.setTextColor(color2)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)
            skintype.clear()
            skintype.add("White skin")
        }

        lightbrown.setOnClickListener {
            paledesc.setTextColor(grey)
            whitedesc.setTextColor(grey)
            moderatedesc.setTextColor(grey)
            lightdesc.setTextColor(lightgrey)
            deepdesc.setTextColor(grey)
            darkdesc.setTextColor(grey)
            pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            paletext.setTextColor(color2)

            whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            whitetext.setTextColor(color2)

            lightbrown.backgroundTintList = ColorStateList.valueOf(blueColor)
            lighttext.setTextColor(color)

            moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            moderatetext.setTextColor(color2)

            deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            deeptext.setTextColor(color2)

            dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            darktext.setTextColor(color2)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)

            skintype.clear()
            skintype.add("Light brown skin")
        }

        moderate.setOnClickListener {
            paledesc.setTextColor(grey)
            whitedesc.setTextColor(grey)
            moderatedesc.setTextColor(lightgrey)
            lightdesc.setTextColor(grey)
            deepdesc.setTextColor(grey)
            darkdesc.setTextColor(grey)
            pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            paletext.setTextColor(color2)

            whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            whitetext.setTextColor(color2)

            lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            lighttext.setTextColor(color2)

            moderate.backgroundTintList = ColorStateList.valueOf(blueColor)
            moderatetext.setTextColor(color)

            deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            deeptext.setTextColor(color2)

            dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            darktext.setTextColor(color2)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)

            skintype.clear()
            skintype.add("Moderate brown skin")
        }

        deep.setOnClickListener {
            paledesc.setTextColor(grey)
            whitedesc.setTextColor(grey)
            moderatedesc.setTextColor(grey)
            lightdesc.setTextColor(grey)
            deepdesc.setTextColor(lightgrey)
            darkdesc.setTextColor(grey)
            pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            paletext.setTextColor(color2)

            whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            whitetext.setTextColor(color2)

            lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            lighttext.setTextColor(color2)

            moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            moderatetext.setTextColor(color2)

            deep.backgroundTintList = ColorStateList.valueOf(blueColor)
            deeptext.setTextColor(color)

            dark.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            darktext.setTextColor(color2)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)

            skintype.clear()
            skintype.add("Deep brown skin")
        }

        dark.setOnClickListener {
            paledesc.setTextColor(grey)
            whitedesc.setTextColor(grey)
            moderatedesc.setTextColor(grey)
            lightdesc.setTextColor(grey)
            deepdesc.setTextColor(grey)
            darkdesc.setTextColor(lightgrey)
            pale.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            paletext.setTextColor(color2)

            whiteskin.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            whitetext.setTextColor(color2)

            lightbrown.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            lighttext.setTextColor(color2)

            moderate.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            moderatetext.setTextColor(color2)

            deep.backgroundTintList = ColorStateList.valueOf(lightBlueColor)
            deeptext.setTextColor(color2)

            dark.backgroundTintList = ColorStateList.valueOf(blueColor)
            darktext.setTextColor(color)
            selectedButton=true
            continue_button.setBackgroundColor(darkBlueColor)
            skintype.clear()
            skintype.add("Dark brown skin")
        }

        continue_button.setOnClickListener {

            if (source == "register") {
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("skin_phototype", skintype.joinToString(","))
                    editor.apply()

                    val savedString = sharedPreferences.getString("skin_phototype", null)


                    val intent = Intent(this, SkinSensitivityFlow::class.java)
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
                        "Please select your skin phototype to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                if (selectedButton == true) {
                    val sharedPreferences =
                        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("skin_phototype", skintype.joinToString(","))
                    editor.apply()




                    val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val concc= SkinPhototype(aux,skintype)
                    val token = sharedPreferences.getString("session_token", null)
                    if (token.isNullOrEmpty()) {
                        Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                    } else {
                    val api = RetrofitInstance.instance
                    api.changeSkinPhotoType("Bearer $token",concc).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
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
                        "Please select your skin phototype to proceed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}