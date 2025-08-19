package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Quote
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainMenuActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainmenu_page)



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_today

        val dateFormat = SimpleDateFormat("MMMM dd, EEEE", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val text_date: TextView=findViewById<TextView>(R.id.text_date)
        text_date.text=currentDate

        val account_button: ImageButton=findViewById<ImageButton>(R.id.image_user)
         account_button.setOnClickListener {
             val intent = Intent(this, UserDetailsActivity::class.java)
             val options = ActivityOptions.makeCustomAnimation(
                 this,
                 R.anim.slide_in_left,
                 R.anim.slide_out_right
             )
             startActivity(intent, options.toBundle())
         }

        val db2= DatabaseManager(this)
        val quote=db2.getQuote()
        val text_quote: TextView=findViewById<TextView>(R.id.text_quote)
        val text_author: TextView=findViewById<TextView>(R.id.text_author)

        if(quote!=null)
        {
            text_quote.text=quote.quote
            text_author.text=quote.author
        }
        else{
            text_quote.text="Unavailable"
            text_author.text="Unavailable"
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        /*

        val token = sharedPreferences.getString("session_token", null)

        val api = RetrofitInstance.instance
        Toast.makeText(this, "Session Token: ${token}", Toast.LENGTH_SHORT).show()
        if (token.isNullOrEmpty()) {
            text_quote.text = "Session token is missing."
            text_author.text = "Please log in to get a quote."
        } else {
            api.getQuote("Bearer $token").enqueue(object : Callback<Quote> {
                override fun onResponse(call: Call<Quote>, response: Response<Quote>) {

                    when (response.code()) {
                        200 -> {
                            val quote = response.body()
                            text_quote.text = quote?.quote
                            text_author.text = quote?.author
                        }

                        else -> {
                            text_quote.text = "The quote is unavilable."
                            text_author.text = "The quote author is unavilable."
                        }
                    }
                }

                override fun onFailure(call: Call<Quote>, t: Throwable) {
                    text_quote.text = "The quote is unavilable."
                    text_author.text = "The quote author is unavilable."
                    Log.e("Error", t.message ?: "Unknown error")
                }
            })
        }
*/

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    true
                }
                R.id.nav_products -> {
                    val intent = Intent(this, ProductsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_recommendations -> {
                    val intent = Intent(this, RecommendationsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                R.id.nav_diary -> {
                    val intent = Intent(this, DiaryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    true
                }
                else -> false
            }

    }


        val button_edit: Button=findViewById<Button>(R.id.editroutines)
        button_edit.setOnClickListener {
            val intent = Intent(this, EditRoutinesActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val dbManager = DatabaseManager(this)
        val savedLog=dbManager.getDailyLogByUserId(aux)

        val db= DatabaseManager(this)
        val x=db.getAllRoutineCompletions()
        if(x!=null) Log.d("debug"," routiness2: "+x.toString())
        val routineList = mutableListOf<UserRoutines>()
        val adapter = AdapterAllRoutines(routineList,this) { item ->
            val intent = Intent(this, RoutineDetailsActivity::class.java)
            intent.putExtra("flag", item.routine_type)
            Log.d("routine",item.routine_type)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
        }
        val recyclerView: RecyclerView=findViewById<RecyclerView>(R.id.recycle)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter



        val savedRoutines = dbManager.getRoutine()
        val savedSpfRoutines = dbManager.getSpf()
        if(savedRoutines.size==0 && savedSpfRoutines==null && savedLog==null)
        {
            val text: TextView=findViewById<TextView>(R.id.textnull)
            text.visibility= View.VISIBLE
        }
        else
        {
            val text: TextView=findViewById<TextView>(R.id.textnull)
            text.visibility= View.GONE
            if(savedLog!=null){
                Log.d("log",savedLog.toString())
                val steps: MutableList<Step> = mutableListOf()
                val logroutine= UserRoutines(savedLog.id,"log",0,"","",steps)
                adapter.addItem(logroutine)
            }
            for (routine in savedRoutines) {

                adapter.addItem(routine)
            }
            if(savedSpfRoutines!=null)
            {
                val steps: MutableList<Step> = mutableListOf()
                val spfroutine= UserRoutines(savedSpfRoutines.id,"spf",0,"","",steps)
                adapter.addItem(spfroutine)
            }


        }


    }
}

