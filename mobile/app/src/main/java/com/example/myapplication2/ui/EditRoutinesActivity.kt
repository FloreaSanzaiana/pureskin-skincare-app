package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DailyLog
import com.example.myapplication2.data.model.DailyLogClass
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SkinType
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditRoutinesActivity: AppCompatActivity() {
    private var routines_id: MutableMap<String, Int> = mutableMapOf()
    private  lateinit  var button: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_routines_page)

         button = findViewById<ImageButton>(R.id.imageButton)

        button.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            intent.putExtra("flag", "register")
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right

            )
            startActivity(intent, options.toBundle())
        }



        val switch_morning = findViewById<SwitchCompat>(R.id.switch_morning)
        val switch_evening = findViewById<SwitchCompat>(R.id.switch_evening)
        val switch_exfoliation = findViewById<SwitchCompat>(R.id.switch_exfoliation)
        val switch_eyemask = findViewById<SwitchCompat>(R.id.switch_eyemask)
        val switch_facemask = findViewById<SwitchCompat>(R.id.switch_facemask)
        val switch_lipmask = findViewById<SwitchCompat>(R.id.switch_lipmask)
        val switch_spf = findViewById<SwitchCompat>(R.id.switch_spf)
        val switch_log=findViewById<SwitchCompat>(R.id.switch_log)


        switch_morning.isChecked = false
        switchColors(switch_morning, isChecked = false)
        switch_evening.isChecked = false
        switchColors(switch_evening, isChecked = false)
        switch_exfoliation.isChecked = false
        switchColors(switch_exfoliation, isChecked = false)
        switch_facemask.isChecked = false
        switchColors(switch_facemask, isChecked = false)
        switch_eyemask.isChecked = false
        switchColors(switch_eyemask, isChecked = false)
        switch_lipmask.isChecked = false
        switchColors(switch_lipmask, isChecked = false)
        switch_spf.isChecked = false
        switchColors(switch_spf, isChecked = false)
        switch_log.isChecked = false
        switchColors(switch_log, isChecked = false)




        val dbManager = DatabaseManager(this)
        val savedRoutines = dbManager.getRoutine()

        for (routine in savedRoutines) {
            when(routine.routine_type){
                "morning" -> {
                    switch_morning.isChecked = true
                    switchColors(switch_morning, isChecked = true)
                    routines_id["morning"]=routine.id
                }
                "evening" ->{
                    switch_evening.isChecked = true
                    switchColors(switch_evening, isChecked = true)
                    routines_id["evening"]=routine.id
                }
                "exfoliation" ->{
                    switch_exfoliation.isChecked = true
                    switchColors(switch_exfoliation, isChecked = true)
                    routines_id["exfoliation"]=routine.id
                }
                "face mask" ->{
                    switch_facemask.isChecked = true
                    switchColors(switch_facemask, isChecked = true)
                    routines_id["face mask"]=routine.id
                }
                "eye mask"->{
                    switch_eyemask.isChecked = true
                    switchColors(switch_eyemask, isChecked = true)
                    routines_id["eye mask"]=routine.id
                }
                "lip mask"->{
                    switch_lipmask.isChecked = true
                    switchColors(switch_lipmask, isChecked = true)
                    routines_id["lip mask"]=routine.id
                }
            }
        }

        val savedSpf = dbManager.getSpf()
        if(savedSpf!=null){
            switch_spf.isChecked = true
            switchColors(switch_spf, isChecked = true)
            routines_id["spf"]=savedSpf.id
        }
        val sharedPreferences =
            this@EditRoutinesActivity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val savedLog=dbManager.getDailyLogByUserId(aux)
        if(savedLog!=null){
            switch_log.isChecked = true
            switchColors(switch_log, isChecked = true)
        }

        switch_morning.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_morning, isChecked)
            if (isChecked) {
                addRoutine("morning")
            }
            else{
                deleteRoutine("morning")
            }
        }

        switch_evening.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_evening, isChecked)

            if (isChecked) {
            addRoutine("evening")
            }
            else{
                deleteRoutine("evening")
            }
        }

        switch_exfoliation.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_exfoliation, isChecked)

            if (isChecked) {
                addRoutine("exfoliation")
            }
            else{
                deleteRoutine("exfoliation")
            }
        }

        switch_facemask.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_facemask, isChecked)

            if (isChecked) {
                addRoutine("face mask")
            }
            else{
                deleteRoutine("face mask")
            }
        }

        switch_eyemask.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_eyemask, isChecked)

            if (isChecked) {
                addRoutine("eye mask")
            }
            else{
                deleteRoutine("eye mask")
            }
        }

        switch_lipmask.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_lipmask, isChecked)

            if (isChecked) {
                addRoutine("lip mask")
            }
            else{
                deleteRoutine("lip mask")
            }
        }

        switch_spf.setOnCheckedChangeListener { buttonView, isChecked ->
            button.isEnabled=false
            switchColors(switch_spf, isChecked)

            if (isChecked) {
                addSpf()
            }
            else{
                deleteSpf()
            }
        }
        switch_log.isEnabled = false
        switch_log.setOnCheckedChangeListener { buttonView, isChecked ->
           /* button.isEnabled=false
            switchColors(switch_log, isChecked)

            if (isChecked) {
                addLog()
            }
            else{
                deleteLog()
            }*/
        }



    }

    fun switchColors(switch: SwitchCompat, isChecked: Boolean) {
        val blue = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
        val gray = ContextCompat.getColor(this, android.R.color.darker_gray)
        val white = ContextCompat.getColor(this, android.R.color.white)

        if (isChecked) {
            switch.trackTintList = ColorStateList.valueOf(blue)
            switch.thumbTintList = ColorStateList.valueOf(white)
        } else {
            switch.trackTintList = ColorStateList.valueOf(gray)
            switch.thumbTintList = ColorStateList.valueOf(white)
        }
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
            button.isEnabled=true
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.addRoutine("Bearer $token",routine).enqueue(object : Callback<UserRoutines> {
                override fun onResponse(call: Call<UserRoutines>, response: Response<UserRoutines>) {
                    button.isEnabled=true
                    when (response.code()) {
                        200 -> {
                            if (response.isSuccessful) {
                                val routine = response.body()
                                if (routine != null) {
                                    val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                    dbManager.insertRoutine(routine)
                                } else {
                                    Log.e("Error", "Response body is null")
                                }

                            }
                        }
                    }
                }
                override fun onFailure(call: Call<UserRoutines>, t: Throwable) {
                    button.isEnabled=true
                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }
    fun deleteRoutine(routine_type: String){
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this routine?")
            .setPositiveButton("Yes") { dialog, which ->


                val routine_id = routines_id[routine_type.lowercase()]?: -1

                val sharedPreferences =
                    this@EditRoutinesActivity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val token = sharedPreferences.getString("session_token", null)

                val routine = Routine(aux, "", "", "", routine_id)

                if (token.isNullOrEmpty()) {
                    button.isEnabled=true
                    Toast.makeText(this@EditRoutinesActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
                } else {
                    val api = RetrofitInstance.instance
                    api.deleteRoutine("Bearer $token", routine).enqueue(object :
                        Callback<com.example.myapplication2.data.model.Response> {
                        override fun onResponse(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            response: Response<com.example.myapplication2.data.model.Response>
                        ) {
                            button.isEnabled=true
                            when (response.code()) {

                                200 -> {
                                    val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                    dbManager.deleteRoutineById(routine_id)
                                    Toast.makeText(this@EditRoutinesActivity, "Routine was successfully deleted.", Toast.LENGTH_SHORT).show() }
                            }
                        }

                        override fun onFailure(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            t: Throwable
                        ) {
                            button.isEnabled=true
                            Log.e("Error", t.message ?: "Unknown error")
                        }
                    })
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun addSpf(){
        val sharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val token = sharedPreferences.getString("session_token", null)
        val spf= SpfRoutine(0,aux,"7:00","19:00",120,"monday,tuesday,wednesday,thursday,friday,saturday,sunday",0)
        if (token.isNullOrEmpty()) {
            button.isEnabled=true
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            val api = RetrofitInstance.instance
            api.addSpf("Bearer $token",spf).enqueue(object : Callback<SpfRoutine> {
                override fun onResponse(call: Call<SpfRoutine>, response: Response<SpfRoutine>) {
                    when (response.code()) {
                        200 -> {
                            button.isEnabled=true

                            if (response.isSuccessful) {
                                val routine = response.body()
                                if (routine != null) {
                                    val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                    dbManager.insertSpf(routine)
                                } else {
                                    Log.e("Error", "Response body is null")
                                }

                            }
                        }
                    }
                }
                override fun onFailure(call: Call<SpfRoutine>, t: Throwable) {
                    button.isEnabled=true
                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}
    }


    fun deleteSpf(){
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this routine?")
            .setPositiveButton("Yes") { dialog, which ->


                val routine_id = routines_id["spf"]?: -1

                val sharedPreferences =
                    this@EditRoutinesActivity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val token = sharedPreferences.getString("session_token", null)

                val routine = SpfRoutine(routine_id,0,"","",0,"",0)

                if (token.isNullOrEmpty()) {
                    button.isEnabled=true
                    Toast.makeText(this@EditRoutinesActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
                } else {
                    button.isEnabled=true
                    val api = RetrofitInstance.instance
                    api.deleteSpf("Bearer $token", routine).enqueue(object :
                        Callback<com.example.myapplication2.data.model.Response> {
                        override fun onResponse(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            response: Response<com.example.myapplication2.data.model.Response>
                        ) {
                            when (response.code()) {

                                200 -> {
                                    val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                    dbManager.deleteSpfRoutines()
                                    Toast.makeText(this@EditRoutinesActivity, "Routine was successfully deleted.", Toast.LENGTH_SHORT).show() }
                            }
                        }

                        override fun onFailure(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            t: Throwable
                        ) {
                            Log.e("Error", t.message ?: "Unknown error")
                        }
                    })
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                button.isEnabled=true
                dialog.dismiss()
            }
            .show()
    }

    fun deleteLog(){
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete Daily Log?")
            .setPositiveButton("Yes") { dialog, which ->



                val sharedPreferences =
                    this@EditRoutinesActivity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val token = sharedPreferences.getString("session_token", null)


                if (token.isNullOrEmpty()) {
                    button.isEnabled=true
                    Toast.makeText(this@EditRoutinesActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
                } else {
                    val api = RetrofitInstance.instance
                    api.deleteDailyLog("Bearer $token", aux).enqueue(object :
                        Callback<com.example.myapplication2.data.model.Response> {
                        override fun onResponse(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            response: Response<com.example.myapplication2.data.model.Response>
                        ) {
                            when (response.code()) {
                                200 -> {
                                    val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                    dbManager.deleteDailyLogByUserId(aux)
                                    Toast.makeText(this@EditRoutinesActivity, "Daily Log was successfully deleted.", Toast.LENGTH_SHORT).show() }
                            }
                            button.isEnabled=true

                        }

                        override fun onFailure(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            t: Throwable
                        ) {
                            button.isEnabled=true
                            Log.e("Error", t.message ?: "Unknown error")
                        }
                    })
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                button.isEnabled=true

                dialog.dismiss()
            }
            .show()
    }
    fun addLog(){

                val sharedPreferences =
                    this@EditRoutinesActivity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val token = sharedPreferences.getString("session_token", null)


                if (token.isNullOrEmpty()) {
                    button.isEnabled=true

                    Toast.makeText(this@EditRoutinesActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
                } else {
                    val api = RetrofitInstance.instance
                    api.addDailyLog("Bearer $token", aux).enqueue(object :
                        Callback<DailyLogClass> {
                        override fun onResponse(
                            call: Call<DailyLogClass>,
                            response: Response<DailyLogClass>
                        ) {
                            when (response.code()) {


                                200 -> {
                                    button.isEnabled=true

                                    val message=response.body()
                                    if(message!=null){
                                        val dbManager = DatabaseManager(this@EditRoutinesActivity)
                                        dbManager.deleteDailyLogByUserId(aux)
                                        dbManager.insertDailyLog(message.user_id,message.id)
                                        Toast.makeText(this@EditRoutinesActivity, "Daily Log was successfully added.", Toast.LENGTH_SHORT).show() }
                                }

                            }
                        }

                        override fun onFailure(
                            call: Call<DailyLogClass>,
                            t: Throwable
                        ) {
                            button.isEnabled=true

                            Log.e("Error", t.message ?: "Unknown error")
                        }
                    })
                }
    }

}
