package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication2.R
import com.google.android.material.card.MaterialCardView
import android.widget.Toast
import com.example.myapplication2.MainActivity
import com.example.myapplication2.data.model.Age
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.ProductsActivity
import com.example.myapplication2.ui.UserDetailsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.account_setting_page)
        val button: ImageButton = findViewById<ImageButton>(R.id.imageButton)

        button.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left

            )
            startActivity(intent, options.toBundle())
        }

        val deleteAccountCard: MaterialCardView = findViewById(R.id.deleteaccount)

        deleteAccountCard.setOnClickListener {
            showDeleteAccountDialog()
        }

        val deleteConversation: MaterialCardView=findViewById(R.id.deleteconversation)
        deleteConversation.setOnClickListener {
            showDeleteConversationDialog()
        }

        val reset_password: MaterialCardView=findViewById(R.id.changepassword)
        reset_password.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val aux=sharedPreferences.getString("email", null)
            var email=""
            if(aux!=null) email=aux
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("flag", "settings")
            intent.putExtra("email_key", email)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left

            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteAccount()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun showDeleteConversationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Chat History")
        builder.setMessage("Are you sure you want to delete all your conversations?\n\n• All messages will be permanently removed\n• Chat history cannot be recovered\n• You can start fresh with new conversations")

        builder.setPositiveButton("Delete All Messages") { dialog, _ ->
            clearChatHistory()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun deleteAccount() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val age=Age(aux,0)
        val api = RetrofitInstance.instance
        val token = sharedPreferences.getString("session_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
        api.deleteUser("Bearer $token",age).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
            override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                when (response.code()) {
                    200 -> {
                        Toast.makeText(this@AccountSettingsActivity, "Account deleted", Toast.LENGTH_SHORT).show()
                        val db= DatabaseManager(this@AccountSettingsActivity)
                        db.deleteAllMessages()
                        db.deleteQuote()
                        db.deleteSpfRoutines()
                        db.deleteAllDailyLogs()
                        db.deleteAllRoutineCompletionDetails()
                        db.deleteAllRoutinesAndSteps()

                    }
                }
            }
            override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {

                Log.e("Error", t.message ?: "Unknown error")
            }

        })}
        sharedPreferences.edit().clear().apply()
         startActivity(Intent(this, MainActivity::class.java))
         finishAffinity()
    }


    private fun clearChatHistory(){
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val api = RetrofitInstance.instance
        val token = sharedPreferences.getString("session_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            api.delete_conversation("Bearer $token",aux).enqueue(object : Callback<com.example.myapplication2.data.model.Response> {
                override fun onResponse(call: Call<com.example.myapplication2.data.model.Response>, response: Response<com.example.myapplication2.data.model.Response>) {
                    when (response.code()) {
                        200 -> {
                            val db= DatabaseManager(this@AccountSettingsActivity)
                            db.deleteAllMessages()
                            Toast.makeText(this@AccountSettingsActivity, "Conversation deleted.", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
                override fun onFailure(call: Call<com.example.myapplication2.data.model.Response>, t: Throwable) {

                    Log.e("Error", t.message ?: "Unknown error")
                }

            })}

    }
}
