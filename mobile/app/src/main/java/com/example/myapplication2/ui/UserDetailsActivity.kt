package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Base64
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.MainActivity
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Item
import com.example.myapplication2.ui.AccountSettingsActivity
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.ui.RegisterActivity


class UserDetailsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details_activity)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val imageBase64 = sharedPreferences.getString("poza_profil", null)
        val email = sharedPreferences.getString("email", null)
        val gendre=sharedPreferences.getString("sex",null)
        val age=sharedPreferences.getInt("varsta",0).toString()
        val concernsString = sharedPreferences.getString("concerns", "")
        val concernsList = concernsString?.split(",")?.toList() ?: emptyList()
        val phototype =sharedPreferences.getString("skin_phototype", "")
        val sensitivty=sharedPreferences.getString("skin_sensitivity", "")
        val skin_type=sharedPreferences.getString("skin_type", "")


        val name_text: TextView=findViewById<TextView>(R.id.textname)
        name_text.text=username.toString()

        if (!imageBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val imageView: ImageView = findViewById(R.id.imageView)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val email_text: TextView=findViewById<TextView>(R.id.textemail)
        email_text.text=email


        val back: ImageButton=findViewById<ImageButton>(R.id.imageBack)
        back.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            startActivity(intent, options.toBundle())
            finish()
        }


        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val itemList = listOf(
            Item("Skin Type", skin_type.toString(), R.drawable.skin),
            Item("Skin sensitivity", sensitivty.toString(), R.drawable.sensitivity),
            Item("Skin phototype", phototype.toString(), R.drawable.phototype),
            Item("Concerns", (concernsList?.size ?: 0).toString() + " concerns", R.drawable.concerns),
            Item("Age", age.toString(), R.drawable.age),
            Item("Gender", if (gendre == "F") "Female" else if (gendre == "M") "Male" else gendre.toString(), R.drawable.gender)
        )

        val itemAdapter = ItemAdapter(
            context = this,
            itemList = itemList,
            hideLastButton = false,
            onClick = { item ->
                when (item.title) {
                    "Skin Type" -> changeSkinType()
                    "Skin sensitivity" -> changeSkinSensitivity()
                    "Skin phototype" -> changeSkinPhototype()
                    "Concerns" -> changeSkinConcerns()
                    "Age" -> changeAge()
                    "Gender" -> changeSex()
                    else -> Toast.makeText(this, "Acțiune necunoscută", Toast.LENGTH_SHORT).show()
                }
            }
        )



        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = itemAdapter



        val recyclerView2: RecyclerView = findViewById(R.id.recyclerView2)

        val itemList2 = listOf(
            Item("Send us an email", "pureskin.florea@gmail.com", R.drawable.mail),
            Item("Account settings", "Delete account, delete chatbot conversation,...", R.drawable.settings),
            Item("Log out", "See you soon!", R.drawable.logout)
        )

        val itemAdapter2 = ItemAdapter(
            context = this,
            itemList = itemList2,
            hideLastButton = true,
            onClick = { item ->
                when (item.title) {
                    "Send us an email" -> trimiteEmail()
                    "Account settings" -> deschideSetariCont()
                    "Log out" -> delogheazaUtilizator()
                    else -> Toast.makeText(this, "Acțiune necunoscută", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView2.layoutManager = LinearLayoutManager(this)
        recyclerView2.adapter = itemAdapter2
    }

    private fun delogheazaUtilizator() {
        val db= DatabaseManager(this)
        db.deleteAllMessages()
        db.deleteQuote()
        db.deleteSpfRoutines()
        db.deleteAllDailyLogs()
        db.deleteAllRoutineCompletionDetails()
        db.deleteAllRoutinesAndSteps()
        deleteNotifications()
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun deschideSetariCont() {
        val intent = Intent(this@UserDetailsActivity, AccountSettingsActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeAge() {
        val intent = Intent(this@UserDetailsActivity, AgeFlowRegister::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeSex() {
        val intent = Intent(this@UserDetailsActivity, IntroFlowRegister::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeSkinType() {
        val intent = Intent(this@UserDetailsActivity, SkinTypeFlow::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeSkinPhototype() {
        val intent = Intent(this@UserDetailsActivity, SkinPhotoTypeFlow::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeSkinConcerns() {
        val intent = Intent(this@UserDetailsActivity, ConcernsFlow::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }
    private fun changeSkinSensitivity() {
        val intent = Intent(this@UserDetailsActivity, SkinSensitivityFlow::class.java)
        intent.putExtra("flag", "user")
        val options = ActivityOptions.makeCustomAnimation(
            this@UserDetailsActivity,
            R.anim.slide_in_left,
            R.anim.slide_out_right

        )
        startActivity(intent, options.toBundle())
    }

    private fun trimiteEmail() {

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("pureskin.florea@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback / Support")
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Trimite email cu..."))
        } catch (ex: Exception) {
            Toast.makeText(this, "Nu există aplicație de email instalată.", Toast.LENGTH_SHORT).show()
        }
        }


    private fun deleteNotifications(){
        val db= DatabaseManager(this)
        val notificationHelper = NotificationHelper(this)
        val routines=db.getRoutine()
        if(routines!=null){
            for(r in routines){
                notificationHelper.cancelRoutineNotifications(r.id)
            }
        }
    }
}