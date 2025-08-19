package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
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
import com.example.myapplication2.data.model.UserRegisterDetails
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.ui.RegisterActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class CommitRegister: AppCompatActivity() {
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.commit_register_page)
        val sh= getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
        val email=sh.getString("email",null)
        val username=sh.getString("username",null)
        val password=sh.getString("password",null)
        val age=sh.getInt("varsta",0)
        val sex=sh.getString("sex",null)
        val type=sh.getString("skin_type",null)
        val phototype=sh.getString("skin_phototype",null)
        val sensitivity=sh.getString("skin_sensitivity",null)
        val concerns=sh.getString("concerns",null)
        val button: Button=findViewById<Button>(R.id.button8)
        val api = RetrofitInstance.instance
        progressBar = findViewById(R.id.progressBar)

        val fimage=drawableToBase64(this, R.drawable.fuser)
        val mimage=drawableToBase64(this,R.drawable.muser)

        button.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            button.isEnabled = false
               val user= UserRegisterDetails(email.toString(),username.toString(),password.toString(),age,sex.toString(),type?.split(",") ?: emptyList(),sensitivity?.split(",") ?: emptyList(),phototype?.split(",") ?: emptyList(),concerns?.split(",") ?: emptyList(),if (sex=="Female") fimage else mimage )

                api.registerWithDetails(user).enqueue(object : Callback<UserDetails> {
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

                                addRoutine("evening")





                            }
                        }
                    }
                    override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        button.isEnabled = true
                        Log.e("Error", t.message ?: "Unknown error")
                    }

})
    }
}

    fun drawableToBase64(context: Context, drawableId: Int): String {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
                                val dbManager = DatabaseManager(this@CommitRegister)
                                dbManager.deleteAllRoutinesAndSteps()
                                for (r in routines) {
                                    Log.d("Routine", "ID: ${r.id}, Step name: ${r.steps}")
                                    dbManager.insertRoutine(r)

                                }
                                getAllMessages()

                            }

                        }
                        else ->{
                            val dbManager = DatabaseManager(this@CommitRegister)
                            dbManager.deleteAllRoutinesAndSteps()

                        }
                    }
                }
                override fun onFailure(call: Call<List<UserRoutines>>, t: Throwable) {
                    val dbManager = DatabaseManager(this@CommitRegister)
                    dbManager.deleteAllRoutinesAndSteps()
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

                                    val dbManager = DatabaseManager(this@CommitRegister)
                                    dbManager.deleteSpfRoutines()
                                    dbManager.insertSpf(routine)
                                    fetchRoutines()
                                }
                            }

                        }
                        else ->{
                            val dbManager = DatabaseManager(this@CommitRegister)
                            dbManager.deleteSpfRoutines()
                        }
                    }
                }
                override fun onFailure(call: Call<SpfRoutine>, t: Throwable) {
                    val dbManager = DatabaseManager(this@CommitRegister)
                    dbManager.deleteSpfRoutines()
                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }

    fun addRoutine(routine_type: String){
        val sharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)
        var routine= Routine(aux.toInt(),routine_type.lowercase().trim(),"9:00","monday,tuesday,wednesday,thursday,friday,saturday,sunday",0)
        if(routine_type=="exfoliation") {
            routine = Routine(
                aux.toInt(),
                routine_type.lowercase().trim(),
                "17:00",
                "saturday",
                0
            )
        }
        if(routine_type=="face mask" || routine_type=="eye mask" || routine_type=="lip mask") {
            routine = Routine(
                aux.toInt(),
                routine_type.lowercase().trim(),
                "17:00",
                "sunday",
                0
            )
        }
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.addRoutine("Bearer $token",routine).enqueue(object : Callback<UserRoutines> {
                override fun onResponse(call: Call<UserRoutines>, response: Response<UserRoutines>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val routine = response.body()
                                if (routine != null) {
                                    val dbManager = DatabaseManager(this@CommitRegister)
                                    dbManager.insertRoutine(routine)
                                    if (routine.routine_type == "morning") {

                                        addSpf()
                                    }
                                    else{
                                        addRoutine("morning")

                                    }
                                } else {
                                    Log.e("Error", "Response body is null")
                                }

                            }
                        }
                    }
                }
                override fun onFailure(call: Call<UserRoutines>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }

    fun addSpf(){
        val sharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)
        val spf= SpfRoutine(0,aux,"7:00","19:00",120,"monday,tuesday,wednesday,thursday,friday,saturday,sunday",0)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.addSpf("Bearer $token",spf).enqueue(object : Callback<SpfRoutine> {
                override fun onResponse(call: Call<SpfRoutine>, response: Response<SpfRoutine>) {
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val routine = response.body()
                                if (routine != null) {
                                    val dbManager = DatabaseManager(this@CommitRegister)
                                    dbManager.insertSpf(routine)
                                    fetchSpf()
                                } else {
                                    Log.e("Error", "Response body is null")
                                }

                            }
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
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        Log.d("API Response", "Produse primite: ${products.size}")

                        val db= DatabaseManager(this@CommitRegister)
                        db.deleteAllProducts()
                        db.insertProducts(products)
                        getAllRoutineCompletions()

                    } else {
                        val db= DatabaseManager(this@CommitRegister)
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
            Toast.makeText(this@CommitRegister, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<UserMessage>> {
                override fun onResponse(call: Call<List<UserMessage>>, response: Response<List<UserMessage>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        Log.d("API Response", "Mesaje primite: ${messages.size}")

                        val db= DatabaseManager(this@CommitRegister)
                        db.deleteAllMessages()
                        db.insertMessages(messages)
                        val m=db.getAllMessages()
                        Log.d("API Response", "Mesaje din bd locala: ${m}")
                        getFilteredProductsFromApi()


                    } else {
                        val db= DatabaseManager(this@CommitRegister)

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
            Toast.makeText(this@CommitRegister, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<RoutineCompletion>> {
                override fun onResponse(call: Call<List<RoutineCompletion>>, response: Response<List<RoutineCompletion>>) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: emptyList()
                        Log.d("API Response", "Mesaje primite: ${messages.size}")

                        val db= DatabaseManager(this@CommitRegister)
                        db.deleteAllRoutineCompletions()
                        db.insertRoutineCompletions(messages)
                        val m=db.getAllRoutineCompletions()
                        Log.d("API Response", "Completed Routines din bd locala: ${m}")

                        getDailyLogs()

                    } else {
                        val db= DatabaseManager(this@CommitRegister)

                        db.deleteAllRoutineCompletions()
                        getDailyLogs()


                    }
                }

                override fun onFailure(call: Call<List<RoutineCompletion>>, t: Throwable) {
                    val db= DatabaseManager(this@CommitRegister)
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
            Toast.makeText(this@CommitRegister, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<DailyLogClass> {
                override fun onResponse(call: Call<DailyLogClass>, response: Response<DailyLogClass>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@CommitRegister)
                            db.deleteAllDailyLogs()
                            db.insertDailyLogClass(messages)
                            val a=db.getTodayDailyLogs(aux)
                            Log.d("smth",messages.toString())
                            getRoutineCompletionDetailsFromServer()
                        }

                    } else {
                        val db= DatabaseManager(this@CommitRegister)

                        db.deleteAllDailyLogs()
                        getRoutineCompletionDetailsFromServer()

                    }
                }

                override fun onFailure(call: Call<DailyLogClass>, t: Throwable) {
                    val db= DatabaseManager(this@CommitRegister)

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
            Toast.makeText(this@CommitRegister, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<RoutineCompletionDetails>> {
                override fun onResponse(call: Call<List<RoutineCompletionDetails>>, response: Response<List<RoutineCompletionDetails>>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@CommitRegister)
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
                        val db= DatabaseManager(this@CommitRegister)

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
                    val db= DatabaseManager(this@CommitRegister)

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
            Toast.makeText(this@CommitRegister, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<Quote> {
                override fun onResponse(call: Call<Quote>, response: Response<Quote>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@CommitRegister)
                            db.deleteQuote()
                            db.insertQuote(messages)
                            Log.d("Quote",db.getQuote().toString())

                            val intent = Intent(this@CommitRegister, MainMenuActivity::class.java)
                            val options = ActivityOptions.makeCustomAnimation(
                                this@CommitRegister,
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                            startActivity(intent, options.toBundle())
                            finishAffinity()
                        }

                    } else {
                        val db= DatabaseManager(this@CommitRegister)
                        db.deleteQuote()
                        Log.d("Quote","error1")

                        val intent = Intent(this@CommitRegister, MainMenuActivity::class.java)
                        val options = ActivityOptions.makeCustomAnimation(
                            this@CommitRegister,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        startActivity(intent, options.toBundle())
                        finishAffinity()
                    }
                }

                override fun onFailure(call: Call<Quote>, t: Throwable) {
                    val db= DatabaseManager(this@CommitRegister)
                    db.deleteQuote()
                    Log.d("Quote","error2")


                    val intent = Intent(this@CommitRegister, MainMenuActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(
                        this@CommitRegister,
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