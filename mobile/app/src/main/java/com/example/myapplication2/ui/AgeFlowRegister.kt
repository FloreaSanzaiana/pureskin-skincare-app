package com.example.myapplication2.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import java.lang.reflect.Field
import androidx.core.graphics.drawable.toDrawable
import com.example.myapplication2.data.model.Age
import com.example.myapplication2.data.model.UserDetails
import com.example.myapplication2.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgeFlowRegister: AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SoonBlockedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.age_flow_page)

        val source = intent.getStringExtra("flag")

        if (source == "register") {
            val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

            button.setOnClickListener {
                val intent = Intent(this, IntroFlowRegister::class.java)
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



        val agePicker: NumberPicker = findViewById(R.id.agePicker)


        agePicker.minValue = 12
        agePicker.maxValue = 100
        agePicker.textSize=100f

        val displayedValues = Array(89) { i -> (12 + i).toString() } // Acum va începe de la 12
        agePicker.displayedValues = displayedValues

        agePicker.wrapSelectorWheel = false
        if (source == "register") {
            agePicker.value = 12
        }
        else{
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val age=sharedPreferences.getInt("varsta",0)
            agePicker.value = age
        }

        agePicker.setOnValueChangedListener { picker, oldVal, newVal ->
            val selectedAge = newVal
        }


        val continue_button=findViewById<Button>(R.id.button8)
        continue_button.setOnClickListener {
            if (source == "register") {


                val sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("varsta", agePicker.value)
                editor.apply()
                val intent = Intent(this, SkinTypeFlow::class.java)
                intent.putExtra("flag", "register")
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())

            }
            else
            {
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("varsta", agePicker.value)
                editor.apply()

                val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val age=Age(aux,agePicker.value)
                val api = RetrofitInstance.instance
                val token = sharedPreferences.getString("session_token", null)
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
                } else {
                api.changeAge("Bearer $token",age).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
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
