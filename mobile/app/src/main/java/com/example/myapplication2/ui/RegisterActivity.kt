package com.example.myapplication2.ui
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.User
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.EditRoutinesActivity
import com.example.myapplication2.ui.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity: AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)
        progressBar = findViewById(R.id.progressBar)
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
            button7.isEnabled = false
            val text2: TextView = findViewById<TextView>(R.id.textView55)



            val api = RetrofitInstance.instance


            val textUsername: EditText =findViewById<EditText>(R.id.textUsername)
            val textEmail: EditText =findViewById<EditText>(R.id.textEmail)
            val textPassword: EditText =findViewById<EditText>(R.id.textPassword1)
            val username = textUsername.text.toString()
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email)) {
                val user = User(0,username, email, password,"","")
                val sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putString("username", username)
                editor.putString("email", email)
                editor.putString("password", email)
                editor.apply()

                progressBar.visibility = View.VISIBLE
                api.addUser(user).enqueue(object : Callback<SessionToken> {
                    override fun onResponse(call: Call<SessionToken>, response: Response<SessionToken>) {
                        text2.visibility = View.VISIBLE

                        when (response.code()) {
                            200 -> {
                                val session = response.body()

                                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("user_id", session?.user_id.toString())
                                editor.apply()


                                text2.setTextColor(Color.parseColor("#00FF00"))
                                text2.text = ""




                                val intent = Intent(this@RegisterActivity, IntroFlowRegister::class.java)
                                intent.putExtra("flag", "register")
                                val options = ActivityOptions.makeCustomAnimation(
                                    this@RegisterActivity,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left

                                )
                                startActivity(intent, options.toBundle())
                                finish()
                            }
                            401 -> {
                                progressBar.visibility = View.GONE
                                button7.isEnabled = true
                                text2.setTextColor(Color.parseColor("#FF0000"))
                                text2.text = "The email is already in use!"
                            }
                            else -> {
                                progressBar.visibility = View.GONE
                                button7.isEnabled = true
                                text2.setTextColor(Color.parseColor("#FF0000"))
                                text2.text = "The service is unavailable. Please try again later."
                            }
                        }
                    }

                    override fun onFailure(call: Call<SessionToken>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        button7.isEnabled = true
                        text2.visibility= View.VISIBLE;
                        text2.setTextColor(Color.parseColor("#FF0000"))
                        text2.text = "The service is unavailable. Please try again later."
                        Log.e("Error", t.message ?: "Unknown error")
                    }
                })
            }

            else if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
                progressBar.visibility = View.GONE
                button7.isEnabled = true
                text2.visibility= View.VISIBLE;
                text2.setTextColor(Color.parseColor("#FF0000"))
                text2.text = "Please fill in all fields."
            }
            else{
                progressBar.visibility = View.GONE
                button7.isEnabled = true
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
}

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}