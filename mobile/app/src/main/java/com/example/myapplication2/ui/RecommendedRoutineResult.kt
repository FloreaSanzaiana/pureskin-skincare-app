package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.ProductRecommended
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.RoutineRecommended
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.EditRoutinesActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendedRoutineResult: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.routine_result_page)
        val sharedPref = getSharedPreferences("UserRoutine", Context.MODE_PRIVATE)
        val json = sharedPref.getString("routine_response", null)

        if (json != null) {
            val type = object : TypeToken<List<RoutineRecommended>>() {}.type
            val routineRecommendedList: List<RoutineRecommended> = Gson().fromJson(json, type)

            val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val adapter = RoutineResultAdapter(routineRecommendedList, this@RecommendedRoutineResult){ routine ->
                openProductDetailsFragment(routine)
            }
            recyclerView.adapter = adapter
        } else {
            Toast.makeText(this, "Nu s-au găsit date.", Toast.LENGTH_SHORT).show()
        }

        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

        button.setOnClickListener {
            val intent = Intent(this, RecommendationsActivity::class.java)
            intent.putExtra("flag", "register")
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }

        val continue_button: Button = findViewById<Button>(R.id.button8)

        continue_button.setOnClickListener {
            val txt=sharedPref.getString("routine_type","")
            val db= DatabaseManager(this)
            if(txt!=null)
            {
                val routine=db.getRoutineByType(txt)
                if(routine!=null){
                    AlertDialog.Builder(this)


                        .setTitle("Replace Routine")
                        .setMessage("This will replace your current routine with the new recommended one. Do you want to continue?")
                        .setPositiveButton("Yes") { _, _ ->
                            if(txt!=null)
                                addRecommendedRoutines(txt,this)
                            val intent = Intent(this, MainMenuActivity::class.java)
                            intent.putExtra("flag", "register")
                            startActivity(intent)
                            finishAffinity()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            val txt=sharedPref.getString("routine_type",null)
                            if(txt!=null)
                                deleteRecommendedRoutines(txt,this)


                            val intent = Intent(this, MainMenuActivity::class.java)
                            intent.putExtra("flag", "register")
                            startActivity(intent)
                            finishAffinity()
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }
                else{
                    AlertDialog.Builder(this)


                        .setTitle("Replace Routine")
                        .setMessage("You don't have this type of routine. Do you want to enable it?")
                        .setPositiveButton("Yes") { _, _ ->
                            if(txt!=null)
                                addRoutine(txt)

                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            val txt=sharedPref.getString("routine_type",null)
                            if(txt!=null)
                                deleteRecommendedRoutines(txt,this)
                            val intent = Intent(this, MainMenuActivity::class.java)
                            intent.putExtra("flag", "register")
                            startActivity(intent)
                            finishAffinity()
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                }


            }

        }
 }

    private fun openProductDetailsFragment(routine: RoutineRecommended) {
        val aux=routine.product
        val product= ProductRecommended(aux.id,aux.product_name,aux.product_type,aux.clean_ingreds,aux.area,aux.time,aux.spf,aux.product_url,aux.price,aux.irritating_ingredients.toString())
        val new_product= Product(product.id,product.product_name,product.product_type,product.clean_ingreds,product.area,product.time,product.spf,product.product_url,product.price,product.irritating_ingredients)
        val fragment = ProductDetailsBottomSheetFragment(new_product)
        fragment.show(supportFragmentManager, fragment.tag)

    }

    private fun deleteRecommendedRoutines(routine_type: String, context: Context) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val x=sharedPreferences.getString("user_id", null)
        var user_id=0
        if(x!=null) user_id=x.toInt()
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("step","User id: "+user_id.toString())
        Log.d("step","Routine Type : "+routine_type.toString())
        val call = apiService.delete_recommended_routine("Bearer $token", user_id,routine_type)
        call.enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
            override fun onResponse(
                call: Call<com.example.myapplication2.data.model.Response>,
                response: Response<com.example.myapplication2.data.model.Response>
            ) {
                if (response.isSuccessful) {
                    Log.d("remove routine","succes")

                } else {
                    Log.d("remove routine","error")
                }
            }

            override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                Toast.makeText(context, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                Log.e("DeleteProduct", "Network error: ${t.message}")
            }
        })
    }

    private fun addRecommendedRoutines(routine_type: String, context: Context) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val x=sharedPreferences.getString("user_id", null)
        var user_id=0
        if(x!=null) user_id=x.toInt()
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("step","User id: "+user_id.toString())
        Log.d("step","Routine Type : "+routine_type.toString())
        val call = apiService.add_recommended_routine("Bearer $token", user_id,routine_type)
        call.enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
            override fun onResponse(
                call: Call<com.example.myapplication2.data.model.Response>,
                response: Response<com.example.myapplication2.data.model.Response>
            ) {
                if (response.isSuccessful) {
                    Log.d("add routine","succes")
                    val db= DatabaseManager(this@RecommendedRoutineResult)
                    val routine=db.getRoutineByType(routine_type)
                    if(routine!=null)
                    {
                        val cod=db.deleteRoutineById(routine.id)
                        getRoutine(routine_type,context)
                    }
                } else {
                    Log.d("add routine","error")
                }
            }

            override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {
                Toast.makeText(context, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                Log.e("DeleteProduct", "Network error: ${t.message}")
            }
        })
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
                                    val dbManager = DatabaseManager(this@RecommendedRoutineResult)
                                    dbManager.insertRoutine(routine)


                                    addRecommendedRoutines(
                                        routine_type,
                                        this@RecommendedRoutineResult
                                    )
                                    val intent = Intent(
                                        this@RecommendedRoutineResult,
                                        MainMenuActivity::class.java
                                    )
                                    intent.putExtra("flag", "register")
                                    startActivity(intent)
                                    finishAffinity()
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

    private fun getRoutine(routine_type: String, context: Context) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val x=sharedPreferences.getString("user_id", null)
        var user_id=0
        if(x!=null) user_id=x.toInt()
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Session token expired.", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("step","User id: "+user_id.toString())
        Log.d("step","Routine Type : "+routine_type.toString())
        val call = apiService.get_recommended_routine("Bearer $token", user_id,routine_type)
        call.enqueue(object : Callback<UserRoutines> {
            override fun onResponse(
                call: Call<UserRoutines>,
                response: Response<UserRoutines>
            ) {
                if (response.isSuccessful) {
                    val routine=response.body()
                    val db= DatabaseManager(this@RecommendedRoutineResult)
                    if(routine!=null)
                    {
                        db.insertRoutine(routine)
                        Log.d("product_id",routine.steps.toString())
                    }

                } else {
                    Log.d("get routine","error")
                }
            }

            override fun onFailure(call: Call<UserRoutines>, t: Throwable) {
                Toast.makeText(context, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                Log.e("DeleteProduct", "Network error: ${t.message}")
            }
        })
    }
}