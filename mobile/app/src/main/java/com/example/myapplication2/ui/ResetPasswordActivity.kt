package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.ResetCode
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.VerifyCodeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password_page)

        var source = intent.getStringExtra("flag").toString()

        val email = intent.getStringExtra("email_key")
        val button: Button=findViewById<Button>(R.id.button4)
        val warrning: TextView=findViewById<TextView>(R.id.textView6)
        val api = RetrofitInstance.instance
        val spinner = findViewById<ProgressBar>(R.id.loadingSpinner)

        button.setOnClickListener {
           warrning.visibility= View.GONE
            val pass: EditText=findViewById<EditText>(R.id.textPassword1)
            val conf: EditText=findViewById<EditText>(R.id.editCode)
            if(pass.text.isNotEmpty() && conf.text.isNotEmpty()){
                spinner.visibility = View.VISIBLE
                button.isEnabled = false
                if(pass.text.toString().trim()==conf.text.toString().trim()){
                    val sendCode= ResetCode(email.toString().trim(),pass.text.toString().trim(),"",java.sql.Date(0))
                    api.changePassword(sendCode).enqueue(object : Callback<ResetCode> {
                        override fun onResponse(call: Call<ResetCode>, responsee: Response<ResetCode>) {
                            when (responsee.code()) {
                                200 ->{

                                    Handler(Looper.getMainLooper()).postDelayed({


                                        if (source=="settings"){
                                            Toast.makeText(this@ResetPasswordActivity, "The password has been saved.", Toast.LENGTH_SHORT).show()

                                            val intent = Intent(this@ResetPasswordActivity,
                                                AccountSettingsActivity::class.java)
                                            val options = ActivityOptions.makeCustomAnimation(
                                                this@ResetPasswordActivity,
                                                R.anim.slide_in_right,
                                                R.anim.slide_out_left
                                            )

                                            startActivity(intent, options.toBundle())
                                        }
                                        else{
                                            spinner.visibility = View.GONE
                                            button.isEnabled = true
                                            val intent = Intent(this@ResetPasswordActivity,
                                                LoginActivity::class.java)
                                            intent.putExtra("email_key", email)
                                            val options = ActivityOptions.makeCustomAnimation(
                                                this@ResetPasswordActivity,
                                                R.anim.slide_in_right,
                                                R.anim.slide_out_left
                                            )

                                            startActivity(intent, options.toBundle())
                                        }

                                    }, 5000)

                                }
                                else -> {
                                    warrning.visibility= View.VISIBLE
                                    spinner.visibility = View.GONE
                                    button.isEnabled = true
                                    warrning.setTextColor(Color.parseColor("#FF0000"))
                                    warrning.text = "The service is unavailable. Please try again later."
                                }
                            }
                        }
                        override fun onFailure(call: Call<ResetCode>, t: Throwable) {

                            spinner.visibility = View.GONE
                            button.isEnabled = true
                            warrning.visibility= View.VISIBLE;
                            warrning.setTextColor(Color.parseColor("#FF0000"))
                            warrning.text = "The service is unavailable. Please try again later."
                            Log.e("Error", t.message ?: "Unknown error")
                        }

                    })

                }else{
                    spinner.visibility = View.GONE
                    button.isEnabled = true
                    warrning.visibility= View.VISIBLE
                    warrning.text="The passwords do not match."
                    warrning.setTextColor(Color.parseColor("#FF0000"))
                }
            }
            else{
                spinner.visibility = View.GONE
                button.isEnabled = true
                warrning.visibility= View.VISIBLE
                warrning.text="Please fill in all fields."
                warrning.setTextColor(Color.parseColor("#FF0000"))
            }
        }
    }
}