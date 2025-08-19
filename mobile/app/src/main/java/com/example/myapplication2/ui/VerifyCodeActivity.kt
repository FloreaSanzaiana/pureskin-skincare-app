package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.icu.text.Edits
import android.os.Bundle
import android.view.TextureView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.ResetCode
import com.example.myapplication2.ui.ForgotPasswordActivity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.myapplication2.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VerifyCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_code_page)
        val email = intent.getStringExtra("email_key")


        val backButton: ImageButton=findViewById<ImageButton>(R.id.imageButton)
        backButton.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )

            startActivity(intent, options.toBundle())
        }
        val code: EditText=findViewById<EditText>(R.id.editCode)
        val warrning: TextView=findViewById<TextView>(R.id.textTest)
        val api = RetrofitInstance.instance
       val verifyButton: Button=findViewById<Button>(R.id.button4)
        val spinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        verifyButton.setOnClickListener {
            warrning.visibility= View.GONE
            verifyButton.isEnabled = false
            if(code.text.isNotEmpty()){
                spinner.visibility = View.VISIBLE

                val sendCode= ResetCode(email.toString().trim(),"",code.text.toString().trim(),java.sql.Date(0))

                api.verifyCode(sendCode).enqueue(object : Callback<ResetCode> {
                    override fun onResponse(call: Call<ResetCode>, responsee: Response<ResetCode>) {
                        when (responsee.code()) {
                            200 ->{


                                Handler(Looper.getMainLooper()).postDelayed({

                                    val intent = Intent(this@VerifyCodeActivity, ResetPasswordActivity::class.java)
                                    intent.putExtra("email_key", email)
                                    val options = ActivityOptions.makeCustomAnimation(
                                        this@VerifyCodeActivity,
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )

                                    startActivity(intent, options.toBundle())
                                }, 5000)

                            }
                            401 -> {
                                warrning.visibility= View.VISIBLE
                                warrning.text="The code has been expired."
                                warrning.setTextColor(Color.parseColor("#FF0000"))
                                verifyButton.isEnabled = true
                                spinner.visibility = View.GONE

                            }
                            400 -> {
                                verifyButton.isEnabled = true
                                spinner.visibility = View.GONE

                                warrning.visibility= View.VISIBLE
                                warrning.text="The code is invalid."
                                warrning.setTextColor(Color.parseColor("#FF0000"))

                            }
                            402 -> {
                                verifyButton.isEnabled = true
                                spinner.visibility = View.GONE
                                warrning.visibility= View.VISIBLE
                                warrning.text="User not found."
                                warrning.setTextColor(Color.parseColor("#FF0000"))

                            }
                            else -> {
                                spinner.visibility = View.GONE
                                verifyButton.isEnabled = true
                                warrning.visibility= View.VISIBLE
                                warrning.setTextColor(Color.parseColor("#FF0000"))
                                warrning.text = "The service is unavailable. Please try again later."
                            }
                        }
                    }
                    override fun onFailure(call: Call<ResetCode>, t: Throwable) {
                        spinner.visibility = View.GONE
                        verifyButton.isEnabled = true
                        warrning.visibility= View.VISIBLE;
                        warrning.setTextColor(Color.parseColor("#FF0000"))
                        warrning.text = "The service is unavailable. Please try again later."
                        Log.e("Error", t.message ?: "Unknown error")
                    }
                })


            }
            else{
                spinner.visibility = View.GONE
                verifyButton.isEnabled = true
                warrning.visibility= View.VISIBLE
                warrning.text="Please fill in all fields."
                warrning.setTextColor(Color.parseColor("#FF0000"))
            }
        }
    }
}