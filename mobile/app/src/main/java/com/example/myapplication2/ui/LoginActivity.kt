package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import android.graphics.Color
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.myapplication2.data.model.DailyLogClass
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.Quote
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.RoutineCompletion
import com.example.myapplication2.data.model.RoutineCompletionDetails
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.User
import com.example.myapplication2.data.model.UserDetails
import com.example.myapplication2.data.model.UserMessage
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.ProductsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)
        progressBar = findViewById(R.id.progressBar)
        var logged=false
        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }

        val button6: Button = findViewById<Button>(R.id.button6)

        button6.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left

            )
            startActivity(intent, options.toBundle())
        }

        val button5: Button = findViewById<Button>(R.id.button5)

        button5.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left

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

            val text2: TextView = findViewById<TextView>(R.id.textView2)
            text2.visibility= View.GONE

            val api = RetrofitInstance.instance


            val textEmail: EditText=findViewById<EditText>(R.id.textEmail)
            val textPassword: EditText=findViewById<EditText>(R.id.textPassword)
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()

            if ( email.isNotEmpty() && password.isNotEmpty()) {
                val user = User(0,"", email, password,"","")
                progressBar.visibility = View.VISIBLE

                api.loginUser(user).enqueue(object : Callback<UserDetails> {
                    override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {

                        when (response.code()) {
                            200 -> {

                                val session = response.body()

                                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()

                                editor.putString("user_id", session?.id.toString())
                                editor.putString("session_token", session?.session_token)
                                editor.putLong("expires_at", session?.expires_at ?: System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                                editor.putString("email", session?.email)
                                editor.putString("username", session?.username)
                                editor.putInt("varsta", session?.varsta ?: 0)
                                editor.putString("sex", session?.sex)
                                editor.putString("skin_type", session?.skin_type?.joinToString(","))
                                editor.putString("skin_sensitivity", session?.skin_sensitivity?.joinToString(","))
                                editor.putString("skin_phototype", session?.skin_phototype?.joinToString(","))
                                editor.putString("concerns", session?.concerns?.joinToString(","))
                                editor.putString("poza_profil", session?.poza_profil)

                                editor.apply()



                                fetchRoutines()

                                logged=true


                            }
                            401 -> {
                                button7.isEnabled = true
                                progressBar.visibility = View.GONE
                                text2.visibility= View.VISIBLE;
                                text2.setTextColor(Color.parseColor("#FF0000"))
                                text2.text = "User not found."
                            }
                            else -> {
                                button7.isEnabled = true
                                progressBar.visibility = View.GONE
                                text2.visibility= View.VISIBLE;
                                text2.setTextColor(Color.parseColor("#FF0000"))
                                text2.text = "!The service is unavailable. Please try again later."
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        button7.isEnabled = true
                        text2.visibility= View.VISIBLE;
                        text2.setTextColor(Color.parseColor("#FF0000"))
                        text2.text = "%The service is unavailable. Please try again later."
                        Log.e("Error", t.message ?: "Unknown error")
                    }
                })
            } else {
                progressBar.visibility = View.GONE
                button7.isEnabled = true
                text2.visibility= View.VISIBLE;
                text2.setTextColor(Color.parseColor("#FF0000"))
                text2.text = "Please fill in all fields."
            }



        }


}
    private fun fetchRoutines(){
        val sharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)


        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.getAllRoutines("Bearer $token",aux).enqueue(object : Callback<List<UserRoutines>> {
                override fun onResponse(call: Call<List<UserRoutines>>, response: Response<List<UserRoutines>>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val routines = response.body() ?: emptyList()
                                val dbManager = DatabaseManager(this@LoginActivity)
                                dbManager.deleteAllRoutinesAndSteps()
                                for (r in routines) {
                                    Log.d("Routine", "ID: ${r.id}, Step name: ${r.steps}")
                                    dbManager.insertRoutine(r)

                                }
                                fetchSpf()
                            }

                        }
                        else ->{
                            val dbManager = DatabaseManager(this@LoginActivity)
                            dbManager.deleteAllRoutinesAndSteps()
                            fetchSpf()

                        }
                    }
                }
                override fun onFailure(call: Call<List<UserRoutines>>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }

    private fun fetchSpf(){
        val sharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)


        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.getSpf("Bearer $token",aux).enqueue(object : Callback<SpfRoutine> {
                override fun onResponse(call: Call<SpfRoutine>, response: Response<SpfRoutine>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val routine = response.body()
                                Log.d("fetchSpf", "Inserted SPF: ")

                                if(routine!=null)
                                {
                                    Log.d("fetchSpf", "Inserted SPF: ${routine}")

                                    val dbManager = DatabaseManager(this@LoginActivity)
                                    dbManager.deleteSpfRoutines()
                                    dbManager.insertSpf(routine)
                                }
                                getAllMessages()

                            }

                        }
                        else ->{
                            val dbManager = DatabaseManager(this@LoginActivity)
                            dbManager.deleteSpfRoutines()
                            getAllMessages()

                        }
                    }
                }
                override fun onFailure(call: Call<SpfRoutine>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }

    private fun getFilteredProductsFromApi() {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)

        val call = apiService.getFilteredProducts("Bearer $token")
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        Log.d("API Response", "Produse primite: ${products.size}")

                       val db= DatabaseManager(this@LoginActivity)
                        db.deleteAllProducts()
                        db.insertProducts(products)
                        getAllRoutineCompletions()

                    } else {
                        val db= DatabaseManager(this@LoginActivity)
                        db.deleteAllProducts()
                        getAllRoutineCompletions()

                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Log.d("API Response", "eroare")
                }
            })
        }}

    private fun getAllMessages(){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.get_all_messages("Bearer $token",aux)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<UserMessage>> {
                override fun onResponse(call: Call<List<UserMessage>>, response: Response<List<UserMessage>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        Log.d("API Response", "Mesaje primite: ${messages.size}")

                        val db= DatabaseManager(this@LoginActivity)
                        db.deleteAllMessages()
                        db.insertMessages(messages)
                        val m=db.getAllMessages()
                        Log.d("API Response", "Mesaje din bd locala: ${m}")
                        getFilteredProductsFromApi()


                    } else {
                        val db= DatabaseManager(this@LoginActivity)

                        db.deleteAllMessages()

                        getFilteredProductsFromApi()

                    }
                }

                override fun onFailure(call: Call<List<UserMessage>>, t: Throwable) {
                    Log.d("API Response", "eroare")
                }
            })
        }
    }

    private fun getAllRoutineCompletions(){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.get_routine_completion("Bearer $token",aux)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<RoutineCompletion>> {
                override fun onResponse(call: Call<List<RoutineCompletion>>, response: Response<List<RoutineCompletion>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        Log.d("API Response", "Mesaje primite: ${messages.size}")

                        val db= DatabaseManager(this@LoginActivity)
                        db.deleteAllRoutineCompletions()
                        db.insertRoutineCompletions(messages)
                        val m=db.getAllRoutineCompletions()
                        Log.d("API Response", "Completed Routines din bd locala: ${m}")

                        getDailyLogs()

                    } else {
                        val db= DatabaseManager(this@LoginActivity)

                        db.deleteAllRoutineCompletions()
                        getDailyLogs()


                    }
                }

                override fun onFailure(call: Call<List<RoutineCompletion>>, t: Throwable) {
                    val db= DatabaseManager(this@LoginActivity)
                    db.deleteAllRoutineCompletions()
                    getDailyLogs()
                }
            })
        }
    }
    private fun getDailyLogs(){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.getDailyLogs("Bearer $token",aux)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<DailyLogClass> {
                override fun onResponse(call: Call<DailyLogClass>, response: Response<DailyLogClass>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@LoginActivity)
                            db.deleteAllDailyLogs()
                            db.insertDailyLogClass(messages)
                            val a=db.getTodayDailyLogs(aux)
                            Log.d("smth",messages.toString())
                            getRoutineCompletionDetailsFromServer()
                        }

                    } else {
                        val db= DatabaseManager(this@LoginActivity)

                        db.deleteAllDailyLogs()
                        getRoutineCompletionDetailsFromServer()

                    }
                }

                override fun onFailure(call: Call<DailyLogClass>, t: Throwable) {
                    val db= DatabaseManager(this@LoginActivity)

                    db.deleteAllDailyLogs()
                }
            })
        }
    }
    private fun getRoutineCompletionDetailsFromServer(){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.getRoutineCompletionDetails("Bearer $token",aux)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<RoutineCompletionDetails>> {
                override fun onResponse(call: Call<List<RoutineCompletionDetails>>, response: Response<List<RoutineCompletionDetails>>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@LoginActivity)
                            db.deleteAllRoutineCompletionDetails()
                            db.insertRoutineCompletionDetailsList(messages)
                            val a=db.getAllRoutineCompletionDetails()
                            Log.d("details",a.toString())
                            getQuote()
                            /*
                            val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                            val options = ActivityOptions.makeCustomAnimation(
                                this@LoginActivity,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                            startActivity(intent, options.toBundle())
                            finishAffinity() */
                        }

                    } else {
                        val db= DatabaseManager(this@LoginActivity)

                        db.deleteAllRoutineCompletionDetails()
                        getQuote()
                       /* val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                        val options = ActivityOptions.makeCustomAnimation(
                            this@LoginActivity,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        startActivity(intent, options.toBundle())
                        finishAffinity()*/

                    }
                }

                override fun onFailure(call: Call<List<RoutineCompletionDetails>>, t: Throwable) {
                    val db= DatabaseManager(this@LoginActivity)

                    db.deleteAllRoutineCompletionDetails()
                    getQuote()
                }
            })
        }
    }



    private fun getQuote(){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.getQuote("Bearer $token")
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<Quote> {
                override fun onResponse(call: Call<Quote>, response: Response<Quote>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@LoginActivity)
                            db.deleteQuote()
                            db.insertQuote(messages)
                            Log.d("Quote",db.getQuote().toString())

                            val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                            val options = ActivityOptions.makeCustomAnimation(
                                this@LoginActivity,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                            startActivity(intent, options.toBundle())
                            finishAffinity()
                        }

                    } else {
                        val db= DatabaseManager(this@LoginActivity)
                        db.deleteQuote()
                        Log.d("Quote","error1")

                        val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                        val options = ActivityOptions.makeCustomAnimation(
                            this@LoginActivity,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        startActivity(intent, options.toBundle())
                        finishAffinity()
                    }
                }

                override fun onFailure(call: Call<Quote>, t: Throwable) {
                    val db= DatabaseManager(this@LoginActivity)
                    db.deleteQuote()
                    Log.d("Quote","error2")

                    val intent = Intent(this@LoginActivity, MainMenuActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        this@LoginActivity,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    startActivity(intent, options.toBundle())
                    finishAffinity()
                }
            })
        }
    }
}
