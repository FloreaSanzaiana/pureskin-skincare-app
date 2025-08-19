package com.example.myapplication2.ui
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.User
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.utils.isValidEmail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity2: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page2)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }

        val button7: Button = findViewById<Button>(R.id.button7)

        button7.setOnClickListener {
            /*val intent = Intent(this, MainMenuActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())*/

            val text2: TextView = findViewById<TextView>(R.id.textView55)

            val textUsername: EditText =findViewById<EditText>(R.id.textUsername)
            val textEmail: EditText =findViewById<EditText>(R.id.textEmail)
            val textPassword: EditText =findViewById<EditText>(R.id.textPassword1)
            val username = textUsername.text.toString()
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email)) {

                val sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putString("username", username)
                editor.putString("email", email)
                editor.putString("password", email)
                editor.apply()

                val intent = Intent(this, IntroFlowRegister::class.java)
                val options = ActivityOptions.makeCustomAnimation(
                    this,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                startActivity(intent, options.toBundle())
            }

            else if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
                text2.visibility= View.VISIBLE;
                text2.setTextColor(Color.parseColor("#FF0000"))
                text2.text = "Please fill in all fields."
            }
            else{
                text2.visibility= View.VISIBLE;
                text2.setTextColor(Color.parseColor("#FF0000"))
                text2.text = "The email address format is invalid."
            }


        }

        val button5: Button = findViewById<Button>(R.id.button5)

        button5.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left

            )
            startActivity(intent, options.toBundle())
        }
    }}