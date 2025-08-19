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
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.ResetCode
import com.example.myapplication2.data.model.User
import com.example.myapplication2.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.LocalDate
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext


class ForgotPasswordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgotpassword_page)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)
        val spinner = findViewById<ProgressBar>(R.id.loadingSpinner)
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }
        val buttonCode: Button = findViewById<Button>(R.id.buttonCode)
        val warrning: TextView = findViewById<TextView>(R.id.text)
        buttonCode.setOnClickListener {
            val text : View?=findViewById<TextView>(android.R.id.message)
            text?.visibility= View.GONE
            val email: EditText=findViewById<EditText>(R.id.editEmail)
            val email_add=email.text.toString().trim()
            if(email_add.length<=0)
            {
                warrning.text="Please fill in all fields."
                warrning.setTextColor(Color.parseColor("#FF0000"))
            }
            else
            {
                spinner.visibility = View.VISIBLE
                buttonCode.isEnabled = false
                val api = RetrofitInstance.instance
                val code= ResetCode(email.text.toString(),"","", java.sql.Date(0))
                api.sendEmailForResetPassword(code).enqueue(object : Callback<ResetCode> {

                    override fun onResponse(call: Call<ResetCode>, response: Response<ResetCode>) {


                        when (response.code()) {
                            200 -> {

                                Handler(Looper.getMainLooper()).postDelayed({


                                    Toast.makeText(this@ForgotPasswordActivity, "We've successfully sent the reset code.", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@ForgotPasswordActivity, VerifyCodeActivity::class.java)
                                    intent.putExtra("email_key", email_add)
                                    val options = ActivityOptions.makeCustomAnimation(
                                        this@ForgotPasswordActivity,
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )

                                    startActivity(intent, options.toBundle())

                                }, 3000)


                            }

                            401 -> {
                                spinner.visibility = View.GONE
                                buttonCode.isEnabled = true

                                warrning.visibility= View.VISIBLE
                                warrning.text="User not found."
                                warrning.setTextColor(Color.parseColor("#FF0000"))


                            }
                            400 -> {
                                spinner.visibility = View.GONE
                                buttonCode.isEnabled = true
                                warrning.visibility= View.VISIBLE
                                warrning.text="Email address not found."
                                warrning.setTextColor(Color.parseColor("#FF0000"))

                            }
                            else -> {
                                spinner.visibility = View.GONE
                                buttonCode.isEnabled = true
                                warrning.visibility= View.VISIBLE
                                warrning.text="The service is unavailable. Please try again later."
                                warrning.setTextColor(Color.parseColor("#FF0000"))

                            }
                        }
                    }

                    override fun onFailure(call: Call<ResetCode>, t: Throwable) {
                        spinner.visibility = View.GONE
                        buttonCode.isEnabled = true
                        warrning.visibility= View.VISIBLE
                        warrning.text="The service is unavailable. Please try again later."
                        warrning.setTextColor(Color.parseColor("#FF0000"))

                        Log.e("Error", t.message ?: "Unknown error")
                    }
                })
            }

        }
}}