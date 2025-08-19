package com.example.myapplication2.ui

import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DailyLogClass
import com.example.myapplication2.data.model.DailyLogContent
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Quote
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SessionToken
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyLogActivity: AppCompatActivity() {
    private lateinit var saveButton: MaterialButton
    private lateinit var skinFeelingSeekBar: SeekBar
    private lateinit var emojiView: TextView
    private lateinit var feelingLabelView: TextView

    private lateinit var stressLevelSeekBar: SeekBar
    private lateinit var stressEmojiView: TextView
    private lateinit var stressLabelView: TextView
    private lateinit var noteText: EditText

    private var skinFeelingScore = 5
    private var stressLevelScore = 5



    private lateinit var normalOption: LinearLayout
    private lateinit var oilyOption: LinearLayout
    private lateinit var dehydratedOption: LinearLayout
    private lateinit var itchyOption: LinearLayout

    private lateinit var sunnyOption: LinearLayout
    private lateinit var cloudyOption: LinearLayout
    private lateinit var precipitationsOption: LinearLayout

    private var selectedSkinCondition : String? = null
    private var selectedWeather: String? = null
    private var stressLevel=0
    private var skinFeelingLevel=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.daily_log_page)
        saveButton = findViewById<MaterialButton>(R.id.saveRoutineButton)
        val back: ImageButton=findViewById<ImageButton>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            startActivity(intent, options.toBundle())
        }

        val scaleUp = ObjectAnimator.ofFloat(saveButton, "scaleX", 1.0f, 1.05f)
        val scaleDown = ObjectAnimator.ofFloat(saveButton, "scaleY", 1.0f, 1.05f)
        scaleUp.duration = 1000
        scaleDown.duration = 1000
        scaleUp.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleUp.repeatMode = ObjectAnimator.REVERSE
        scaleDown.repeatMode = ObjectAnimator.REVERSE

        scaleUp.start()
        scaleDown.start()

        skinFeelingSeekBar = findViewById(R.id.skinFeelingSeekBar)
        emojiView = findViewById(R.id.emojiView)
        feelingLabelView = findViewById(R.id.feelingLabelView)
        noteText=findViewById<EditText>(R.id.notesEditText)

        normalOption = findViewById(R.id.normalOption)
        oilyOption = findViewById(R.id.oilyOption)
        dehydratedOption = findViewById(R.id.dehydratedOption)
        itchyOption = findViewById(R.id.itchyOption)
        sunnyOption = findViewById(R.id.sunnyOption)
        cloudyOption = findViewById(R.id.cloudyOption)
        precipitationsOption = findViewById(R.id.precipitationsOption)

        stressLevelSeekBar = findViewById(R.id.stressFeelingSeekBar)
        stressEmojiView = findViewById(R.id.emojiStress)
        stressLabelView = findViewById(R.id.stressLabelView)

        updateEmojiAndLabel(5)
        updateStressEmojiAndLabel(5)

        completeFromDataBase()



    }
    private fun setupListeners() {
        skinFeelingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                skinFeelingScore = progress
                updateEmojiAndLabel(progress)

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        stressLevelSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                stressLevelScore = progress
                updateStressEmojiAndLabel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            Log.d("log","Stress level: "+stressLevel.toString())
            Log.d("log","Skin level: "+skinFeelingLevel.toString())
            Log.d("log","Text: "+noteText.text)
            Log.d("log", "Skin condition: $selectedSkinCondition")
            Log.d("log", "Weather: $selectedWeather")

            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val user_id=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
            val db= DatabaseManager(this)
            val log=db.getDailyLogByUserId(user_id)
            var log_id=0
            if(log!=null) log_id=log.id



            if( selectedWeather==null && selectedSkinCondition==null && noteText.text.isEmpty()){
                Toast.makeText(this@DailyLogActivity, "Add some details to save your daily log.", Toast.LENGTH_SHORT).show()
                saveButton.visibility= View.VISIBLE

            }
            else if( selectedWeather==null || selectedSkinCondition==null || noteText.text.isEmpty())
                AlertDialog.Builder(this@DailyLogActivity)
                .setTitle("Incomplete Entry")
                .setMessage("Some fields are missing. Would you like to save your log anyway?")
                .setPositiveButton("Save") { _, _ ->
                    val aux = DailyLogContent(
                        0,
                        log_id,
                        skinFeelingLevel,
                        selectedSkinCondition?.toString()?.lowercase() ?: "",
                        noteText.text.toString(),
                        selectedWeather,
                        stressLevel.toInt(),
                        ""
                    )
                    insertDailyLogs(aux)
                    saveButton.visibility= View.GONE

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            else{
                showFinalConfirmationDialog(log_id)

            }


        }
        setupCardListeners()
    }
    private fun updateEmojiAndLabel(progress: Int) {
        val emoji = when (progress) {
            0, 1, 2, 3 -> "😢"
            4, 5, 6 -> "😐"
            7, 8, 9, 10 -> "🥰"
            else -> "😐"
        }

        val label = when (progress) {
            0, 1, 2, 3 -> "Bad"
            4, 5, 6 -> "Normal"
            7, 8, 9, 10 -> "Good"
            else -> "Normal"
        }
        if(label=="Bad")
            skinFeelingLevel=3
        else if(label=="Normal")
            skinFeelingLevel=6
        else
            skinFeelingLevel=10

        emojiView.text = emoji
        feelingLabelView.text = label
    }

    private fun updateStressEmojiAndLabel(progress: Int) {
        val emoji = when (progress) {
            0, 1, 2, 3 -> "😌"
            4, 5, 6 -> "😐"
            7, 8, 9, 10 -> "😰"
            else -> "😐"
        }

        val label = when (progress) {
            0, 1, 2, 3 -> "Calm"
            4, 5, 6 -> "Normal"
            7, 8, 9, 10 -> "Stressed"
            else -> "Normal"
        }
        if(label=="Stressed")
            stressLevel=10
        else if(label=="Normal")
            stressLevel=6
        else
            stressLevel=3

        stressEmojiView.text = emoji
        stressLabelView.text = label
    }

    private fun selectSkinCondition(condition: String, selectedView: LinearLayout) {
        normalOption.isSelected = false
        oilyOption.isSelected = false
        dehydratedOption.isSelected = false
        itchyOption.isSelected = false

        selectedView.isSelected = true
        selectedSkinCondition = condition
        selectedView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }
    private fun setupCardListeners() {
        sunnyOption.setOnClickListener {
            selectWeather("sunny", sunnyOption)
        }

        cloudyOption.setOnClickListener {
            selectWeather("cloudy", cloudyOption)
        }

        precipitationsOption.setOnClickListener {
            selectWeather("precipitations", precipitationsOption)
        }

        normalOption.setOnClickListener {
            selectSkinCondition("normal", normalOption)
        }

        oilyOption.setOnClickListener {
            selectSkinCondition("oily", oilyOption)
        }

        dehydratedOption.setOnClickListener {
            selectSkinCondition("dehydrated", dehydratedOption)
        }

        itchyOption.setOnClickListener {
            selectSkinCondition("itchy", itchyOption)
        }
    }
    private fun selectWeather(weather: String, selectedView: LinearLayout) {
        sunnyOption.isSelected = false
        cloudyOption.isSelected = false
        precipitationsOption.isSelected = false

        selectedView.isSelected = true
        selectedWeather = weather

        selectedView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun insertDailyLogs(content: DailyLogContent){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val call = apiService.insertDailyLogsContent("Bearer $token",content)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@DailyLogActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<DailyLogContent> {
                override fun onResponse(call: Call<DailyLogContent>, response: Response<DailyLogContent>) {
                    if (response.isSuccessful) {
                        val messages = response.body()
                        if(messages!=null){
                            val db= DatabaseManager(this@DailyLogActivity)
                            db.insertDailyLogContent(messages)
                            Toast.makeText(this@DailyLogActivity, "Daily log saved!", Toast.LENGTH_SHORT).show()

                        }

                    }
                }

                override fun onFailure(call: Call<DailyLogContent>, t: Throwable) {

                    Toast.makeText(this@DailyLogActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun completeFromDataBase(){
        val db= DatabaseManager(this)
        val r=db.getTodayDailyLogContent()
        if(r!=null){
            disableAllControls()
           saveButton.visibility= View.GONE
            r.skin_feeling_score?.let { score ->
                val seekBarValue = when(score) {
                    3 -> 2  // bad (0-3)
                    6 -> 5  // normal (4-6)
                    10 -> 8 // good (7-10)
                    else -> score
                }
                skinFeelingSeekBar.progress = seekBarValue
                updateEmojiAndLabel(seekBarValue)
            }

            r.stress_level?.let { stress ->
                val seekBarValue = when(stress) {
                    3 -> 2   // stressed (0-3)
                    6 -> 5   // normal (4-6)
                    10 -> 8  // calm (7-10)
                    else -> stress
                }
                stressLevelSeekBar.progress = seekBarValue
                updateStressEmojiAndLabel(seekBarValue)
            }

            if(!r.notes.isNullOrEmpty()) {
                noteText.setText(r.notes)
            }

            if(!r.skin_condition.isNullOrEmpty()) {
                when(r.skin_condition.lowercase()) {
                    "normal" -> selectSkinCondition("normal", normalOption)
                    "oily" -> selectSkinCondition("oily", oilyOption)
                    "dehydrated" -> selectSkinCondition("dehydrated", dehydratedOption)
                    "itchy" -> selectSkinCondition("itchy", itchyOption)
                }
            }

            if(!r.weather.isNullOrEmpty()) {
                when(r.weather.lowercase()) {
                    "sunny" -> selectWeather("sunny", sunnyOption)
                    "cloudy" -> selectWeather("cloudy", cloudyOption)
                    "precipitations" -> selectWeather("precipitations", precipitationsOption)
                }
            }

            Log.d("DailyLog", "Data loaded from database: skin_feeling=${r.skin_feeling_score}, stress=${r.stress_level}, notes=${r.notes}, skin_condition=${r.skin_condition}, weather=${r.weather}")
        }

        else{
            setupListeners()

            saveButton.visibility= View.VISIBLE

        }
    }
    private fun disableAllControls() {

        noteText.isEnabled = false
        noteText.isFocusable = false
        noteText.isFocusableInTouchMode = false

        normalOption.isClickable = false
        oilyOption.isClickable = false
        dehydratedOption.isClickable = false
        itchyOption.isClickable = false

        sunnyOption.isClickable = false
        cloudyOption.isClickable = false
        precipitationsOption.isClickable = false


    }

    private fun showFinalConfirmationDialog(log_id: Int) {
        AlertDialog.Builder(this@DailyLogActivity)
            .setTitle("Save Daily Log?")
            .setMessage("Once saved, your daily log cannot be edited. Are you ready to finalize today's entry?")
            .setPositiveButton("Save Entry") { _, _ ->
                val aux= DailyLogContent(0,log_id,skinFeelingLevel,selectedSkinCondition.toString().lowercase(),noteText.text.toString(),selectedWeather,stressLevel.toInt(),"")

                insertDailyLogs(aux)
                saveButton.visibility= View.GONE
            }
            .setNegativeButton("Keep Editing") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
