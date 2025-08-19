package com.example.myapplication2.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.Product
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SpfRoutine
import com.example.myapplication2.data.model.Step
import com.example.myapplication2.data.model.UserRoutines
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.EditRoutinesActivity
import com.example.myapplication2.util.ImageSetter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.pm.PackageManager
import com.example.myapplication2.MainActivity
import androidx.core.app.NotificationManagerCompat
import androidx.appcompat.app.AlertDialog
import android.Manifest

class NotificationBottomPage(private var routine: Routine, private var spf: SpfRoutine? = null) : BottomSheetDialogFragment() {
    private lateinit var timeText: TextView
    private val selectedDays = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(spf!=null)
            return inflater.inflate(R.layout.notification_spf_bottom_page, container, false)
        else
            return inflater.inflate(R.layout.notification_bottom_page, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(spf==null) {


            val back_image: ImageView = view.findViewById<ImageView>(R.id.image)
            val text: TextView = view.findViewById<TextView>(R.id.name)
            val description: TextView = view.findViewById<TextView>(R.id.description)
            timeText = view.findViewById<TextView>(R.id.timeText)

            val dayViews = mapOf(
                "monday" to view.findViewById<TextView>(R.id.dayMonday),
                "tuesday" to view.findViewById<TextView>(R.id.dayTuesday),
                "wednesday" to view.findViewById<TextView>(R.id.dayWednesday),
                "thursday" to view.findViewById<TextView>(R.id.dayThursday),
                "friday" to view.findViewById<TextView>(R.id.dayFriday),
                "saturday" to view.findViewById<TextView>(R.id.daySaturday),
                "sunday" to view.findViewById<TextView>(R.id.daySunday)
            )

            val minusButton: ImageView = view.findViewById<ImageView>(R.id.minusButton)
            val plusButton: ImageView = view.findViewById<ImageView>(R.id.plusButton)

            minusButton.setOnClickListener {
                updateTime(-1)
            }
            plusButton.setOnClickListener {
                updateTime(30)
            }

            fun loadDaysFromDatabase() {
                if (routine.notify_days.isNotEmpty()) {
                    val cleanString = routine.notify_days
                        .replace("{", "")
                        .replace("}", "")
                        .replace("'", "")

                    val daysFromDb = cleanString.split(",")
                    daysFromDb.forEach { day ->
                        val trimmedDay = day.trim().lowercase()
                        selectedDays.add(trimmedDay)
                        dayViews[trimmedDay]?.isSelected = true
                    }
                }
                Log.d("days","zile din bd: "+selectedDays.toString())
                updateRoutineDays()

            }

            fun loadTimeFromDatabase() {
                if (routine.notify_time.isNotEmpty()) {
                    val timeWithoutSeconds = routine.notify_time.substring(0, 5)
                    val timeParts = timeWithoutSeconds.split(":")
                    val hour = timeParts[0].padStart(2, '0')
                    val minute = timeParts[1].padStart(2, '0')
                    val formattedTime = "$hour:$minute"
                    timeText.text = formattedTime
                }
            }

            dayViews.forEach { (dayName, textView) ->
                textView.setOnClickListener {
                    if (selectedDays.contains(dayName)) {
                        selectedDays.remove(dayName)
                        textView.isSelected = false
                    } else {
                        selectedDays.add(dayName)
                        textView.isSelected = true
                    }

                    updateRoutineDays()

                    Log.d("SelectedDays", "Zilele selectate: ${selectedDays.joinToString(",")}")
                }
            }

            val delete_button: ImageButton = view.findViewById<ImageButton>(R.id.delete)
            delete_button.setOnClickListener {
                val notificationHelper = NotificationHelper(requireContext())
                notificationHelper.cancelRoutineNotifications(routine.routine_id)
                Toast.makeText(requireContext(), "Notifications deleted.", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }
            val save_button: Button = view.findViewById<Button>(R.id.save_button)

            save_button.setOnClickListener {
                Log.d("days",selectedDays.size.toString()+"   "+selectedDays.toString())
                if(selectedDays.size<1 || selectedDays.size<2 && selectedDays.first()=="set()" )
                {
                    Toast.makeText(requireContext(), "Please select at least a day to set up the notifications.", Toast.LENGTH_SHORT)
                        .show()
                }
                else
                {
                    updateRoutineBeforeSaving()

                    val sharedPreferences =
                        requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                    val token = sharedPreferences.getString("session_token", null)

                    if (token.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "Session token expired.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val api = RetrofitInstance.instance
                        api.modify_time("Bearer $token", routine).enqueue(object :
                            Callback<com.example.myapplication2.data.model.Response> {
                            override fun onResponse(
                                call: Call<com.example.myapplication2.data.model.Response>,
                                response: Response<com.example.myapplication2.data.model.Response>
                            ) {
                                when (response.code()) {
                                    200 -> {


                                        val dbManager = DatabaseManager(requireContext())
                                        val success = dbManager.updateRoutineNotifications(
                                            routine.routine_id,
                                            routine.notify_time,
                                            routine.notify_days
                                        )

                                        if (success) {

                                            if (hasNotificationPermissions()) {
                                                val notificationHelper = NotificationHelper(requireContext())
                                                notificationHelper.cancelRoutineNotifications(routine.routine_id)
                                                notificationHelper.scheduleRoutineNotifications(routine)
                                                Toast.makeText(requireContext(), "Notifications have been updated.", Toast.LENGTH_SHORT).show()
                                                dismiss()
                                            } else {
                                                showNotificationPermissionDialog()
                                            }

                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to update local database.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    else -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Something went wrong.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
            }

            when (routine.routine_type) {
                "morning" -> {
                    text.text = "Morning Routine"
                    description.text =
                        "This routine cleanses, protects, and energizes your skin for whatever comes next."
                    back_image.setImageResource(R.drawable.sun)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }

                "evening" -> {
                    text.text = "Evening Routine"
                    description.text =
                        "This nighttime ritual removes the day's buildup and lets your skin recover while you sleep."
                    back_image.setImageResource(R.drawable.moon)
                    Log.d("days", routine.notify_days)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }

                "exfoliation" -> {
                    text.text = "Face Exfoliation"
                    description.text =
                        "Gentle exfoliation removes dead skin cells and unclogs pores for smoother, brighter skin."
                    back_image.setImageResource(R.drawable.exfoliation)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }

                "face mask" -> {
                    text.text = "Face Mask"
                    description.text =
                        "Face masks deliver concentrated care for specific skin concerns and instant results."
                    back_image.setImageResource(R.drawable.face_mask_icon)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }

                "eye mask" -> {
                    text.text = "Eye Mask"
                    description.text =
                        "These targeted treatments reduce puffiness, dark circles, and fine lines around delicate eye area."
                    back_image.setImageResource(R.drawable.eye_mask_icon)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }

                "lip mask" -> {
                    text.text = "Lip Mask"
                    description.text =
                        "Lip masks deeply hydrate and repair for soft, smooth, kissable lips all day long."
                    back_image.setImageResource(R.drawable.lip_mask)
                    loadDaysFromDatabase()
                    loadTimeFromDatabase()
                }


            }
        }
        else{
            val back_image: ImageView = view.findViewById<ImageView>(R.id.image)
            val text: TextView = view.findViewById<TextView>(R.id.name)
            val description: TextView = view.findViewById<TextView>(R.id.description)

            text.text = "Sunscreen Reminder"
            description.text = "Consistent SPF protection prevents sun damage and keeps your skin looking young."
            back_image.setImageResource(R.drawable.sunscreen_icon)

            timeText = view.findViewById<TextView>(R.id.timeText)
            val timeText2: TextView = view.findViewById<TextView>(R.id.timeText2)
            val timeText3: TextView = view.findViewById<TextView>(R.id.timeText3)

            val minusButton: ImageView = view.findViewById<ImageView>(R.id.minusButton)
            val plusButton: ImageView = view.findViewById<ImageView>(R.id.plusButton)

            val minusButton2: ImageView = view.findViewById<ImageView>(R.id.minusButton2)
            val plusButton2: ImageView = view.findViewById<ImageView>(R.id.plusButton2)

            val minusButton3: ImageView = view.findViewById<ImageView>(R.id.minusButton3)
            val plusButton3: ImageView = view.findViewById<ImageView>(R.id.plusButton3)

            minusButton.setOnClickListener {
                updateSpfStartTime(-30)
            }
            plusButton.setOnClickListener {
                updateSpfStartTime(30)
            }

            minusButton2.setOnClickListener {
                updateSpfEndTime(-30, timeText2)
            }
            plusButton2.setOnClickListener {
                updateSpfEndTime(30, timeText2)
            }

            minusButton3.setOnClickListener {
                updateSpfInterval(-30, timeText3)
            }
            plusButton3.setOnClickListener {
                updateSpfInterval(30, timeText3)
            }

            val dayViews = mapOf(
                "monday" to view.findViewById<TextView>(R.id.dayMonday),
                "tuesday" to view.findViewById<TextView>(R.id.dayTuesday),
                "wednesday" to view.findViewById<TextView>(R.id.dayWednesday),
                "thursday" to view.findViewById<TextView>(R.id.dayThursday),
                "friday" to view.findViewById<TextView>(R.id.dayFriday),
                "saturday" to view.findViewById<TextView>(R.id.daySaturday),
                "sunday" to view.findViewById<TextView>(R.id.daySunday)
            )

            dayViews.forEach { (dayName, textView) ->
                textView.setOnClickListener {
                    if (selectedDays.contains(dayName)) {
                        selectedDays.remove(dayName)
                        textView.isSelected = false
                    } else {
                        selectedDays.add(dayName)
                        textView.isSelected = true
                    }
                    updateSpfDays()
                    Log.d("SelectedDays", "SPF zilele selectate: ${selectedDays.joinToString(",")}")
                }
            }

            loadSpfDataFromDatabase(dayViews, timeText2, timeText3)

            val delete_button: ImageButton = view.findViewById<ImageButton>(R.id.delete)
            delete_button.setOnClickListener {
                val notificationHelper = NotificationHelper(requireContext())
                val currentSpf=spf
                if(currentSpf!=null)
                {
                    notificationHelper.cancelSpfNotifications(currentSpf.id)
                    Toast.makeText(requireContext(), "SPF notifications deleted.", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }

            val save_button: Button = view.findViewById<Button>(R.id.save_button)
            save_button.setOnClickListener {
                saveSpfSettings(timeText2, timeText3)


                val sharedPreferences =
                    requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
                val token = sharedPreferences.getString("session_token", null)
                val currentSpf = spf
                if (token.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Session token expired.", Toast.LENGTH_SHORT)
                        .show()
                } else if(currentSpf!=null){
                    val api = RetrofitInstance.instance
                    api.modify_spf_time("Bearer $token", currentSpf).enqueue(object :
                        Callback<com.example.myapplication2.data.model.Response> {
                        override fun onResponse(
                            call: Call<com.example.myapplication2.data.model.Response>,
                            response: Response<com.example.myapplication2.data.model.Response>
                        ) {
                            when (response.code()) {
                                200 -> {


                                    val dbManager = DatabaseManager(requireContext())
                                    val success = dbManager.updateSpfRoutine(
                                       currentSpf
                                    )

                                    if (success) {

                                        if (hasNotificationPermissions()) {
                                            val notificationHelper =
                                                NotificationHelper(requireContext())
                                            notificationHelper.cancelSpfNotifications(currentSpf.id)
                                            notificationHelper.scheduleSpfNotifications(currentSpf)

                                            Toast.makeText(
                                                requireContext(),
                                                "Notifications updated!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dismiss()
                                        } else {
                                            showNotificationPermissionDialog()
                                        }

                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to update local database.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
        }
    }

    /*
     else -> {
                    text.text = "Sunscreen Reminder"
                    description.text =
                        "Consistent SPF protection prevents sun damage and keeps your skin looking young."
                    back_image.setImageResource(R.drawable.sunscreen_icon)
                    val dbManager = DatabaseManager(requireContext())
                }
     */
    private fun updateTime(addMinutes: Int) {
        val currentTime = timeText.text.toString()
        val timeParts = currentTime.split(":")
        val currentHour = timeParts[0].toInt()
        val currentMinute = timeParts[1].toInt()

        var totalMinutes = currentHour * 60 + currentMinute + addMinutes

        if (totalMinutes < 0) {
            totalMinutes += 24 * 60
        } else if (totalMinutes >= 24 * 60) {
            totalMinutes -= 24 * 60
        }

        val newHour = totalMinutes / 60
        val newMinute = totalMinutes % 60

        val formattedTime = String.format("%02d:%02d", newHour, newMinute)
        timeText.text = formattedTime

        updateRoutineTime()
    }

    private fun updateRoutineDays() {
        val daysString = selectedDays.joinToString(",") { "'$it'" }
        routine.notify_days = "{$daysString}"
        Log.d("UpdatedRoutine", "Zile actualizate: ${routine.notify_days}")
    }

    private fun updateRoutineTime() {
        val currentTime = timeText.text.toString()
        routine.notify_time = "$currentTime:00"
        Log.d("UpdatedRoutine", "Oră actualizată: ${routine.notify_time}")
    }

    private fun updateRoutineBeforeSaving() {
        updateRoutineDays()
        updateRoutineTime()
        Log.d("FinalRoutine", "Rutina finală: zile=${routine.notify_days}, oră=${routine.notify_time}")
    }
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = requireContext().getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)) {
                Log.d("BatteryOptimization", "Requesting battery optimization exemption...")

                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.fromParts("package",requireContext().packageName, null)
                }

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("BatteryOptimization", "Failed to open battery settings: ${e.message}")

                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(fallbackIntent)
                }
            } else {
                Log.d("BatteryOptimization", "App is already exempt from battery optimization")
            }
        }
    }


    private fun hasNotificationPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
            }
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Notifications")
            .setMessage("To receive skincare routine reminders, this app needs notification permission. Would you like to enable it now?")
            .setPositiveButton("Enable") { _, _ ->
                val activity = requireActivity()
                if (activity is MainActivity) {
                    activity.ensureNotificationsEnabled {
                        if (spf != null) {
                            val notificationHelper = NotificationHelper(requireContext())
                            notificationHelper.cancelSpfNotifications(spf!!.id)
                            notificationHelper.scheduleSpfNotifications(spf!!)
                            Toast.makeText(requireContext(), "SPF notifications updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            val notificationHelper = NotificationHelper(requireContext())
                            notificationHelper.cancelRoutineNotifications(routine.routine_id)
                            notificationHelper.scheduleRoutineNotifications(routine)
                            Toast.makeText(requireContext(), "Notifications updated!", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                } else {
                    openNotificationSettings()
                }
            }
            .setNegativeButton("Skip") { _, _ ->
                val message = if (spf != null) "SPF routine saved without notifications" else "Routine saved without notifications"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
            }
        }
        startActivity(intent)

        Toast.makeText(requireContext(), "Please enable notifications and try again", Toast.LENGTH_LONG).show()
        dismiss()
    }


    private fun loadSpfDataFromDatabase(dayViews: Map<String, TextView>, timeText2: TextView, timeText3: TextView) {
        spf?.let { spfRoutine ->
            if (spfRoutine.start_time.isNotEmpty()) {
                val timeParts = spfRoutine.start_time.split(":")
                val hour = timeParts[0].padStart(2, '0')
                val minute = timeParts[1].padStart(2, '0')
                val formattedTime = "$hour:$minute"
                timeText.text = formattedTime
            }

            if (spfRoutine.end_time.isNotEmpty()) {
                val timeParts = spfRoutine.end_time.split(":")
                val hour = timeParts[0].padStart(2, '0')
                val minute = timeParts[1].padStart(2, '0')
                val formattedTime = "$hour:$minute"
                timeText2.text = formattedTime
            }

            timeText3.text = spfRoutine.interval_minutes.toString()

            if (spfRoutine.active_days.isNotEmpty()) {
                val daysFromDb = spfRoutine.active_days.split(",")
                daysFromDb.forEach { day ->
                    val trimmedDay = day.trim().lowercase()
                    selectedDays.add(trimmedDay)
                    dayViews[trimmedDay]?.isSelected = true
                }
            }
        }
    }
    private fun updateSpfStartTime(addMinutes: Int) {
        val currentTime = timeText.text.toString()
        val timeParts = currentTime.split(":")
        val currentHour = timeParts[0].toInt()
        val currentMinute = timeParts[1].toInt()

        var totalMinutes = currentHour * 60 + currentMinute + addMinutes

        if (totalMinutes < 0) {
            totalMinutes += 24 * 60
        } else if (totalMinutes >= 24 * 60) {
            totalMinutes -= 24 * 60
        }

        val newHour = totalMinutes / 60
        val newMinute = totalMinutes % 60

        val formattedTime = String.format("%02d:%02d", newHour, newMinute)
        timeText.text = formattedTime
    }

    private fun updateSpfEndTime(addMinutes: Int, timeText2: TextView) {
        val currentTime = timeText2.text.toString()
        val timeParts = currentTime.split(":")
        val currentHour = timeParts[0].toInt()
        val currentMinute = timeParts[1].toInt()

        var totalMinutes = currentHour * 60 + currentMinute + addMinutes

        if (totalMinutes < 0) {
            totalMinutes += 24 * 60
        } else if (totalMinutes >= 24 * 60) {
            totalMinutes -= 24 * 60
        }

        val newHour = totalMinutes / 60
        val newMinute = totalMinutes % 60

        val formattedTime = String.format("%02d:%02d", newHour, newMinute)
        timeText2.text = formattedTime
    }

    private fun updateSpfInterval(addMinutes: Int, timeText3: TextView) {
        val currentInterval = timeText3.text.toString().trim().toIntOrNull() ?: 120
        var newInterval = currentInterval + addMinutes

        if (newInterval < 60) newInterval = 60
        if (newInterval > 480) newInterval = 480

        timeText3.text = newInterval.toString()
    }

    private fun updateSpfDays() {
        val daysString = selectedDays.joinToString(", ")
        spf?.active_days = daysString
        Log.d("UpdatedSpf", "SPF zile actualizate: ${spf?.active_days}")
    }

    private fun saveSpfSettings(timeText2: TextView, timeText3: TextView) {
        updateSpfDays()

        spf?.let { spfRoutine ->
            spfRoutine.start_time = "${timeText.text}:00"
            spfRoutine.end_time = "${timeText2.text}:00"
            spfRoutine.interval_minutes = timeText3.text.toString().toIntOrNull() ?: 120

            Log.d("FinalSpf", "SPF finală: start=${spfRoutine.start_time}, end=${spfRoutine.end_time}, interval=${spfRoutine.interval_minutes}, zile=${spfRoutine.active_days}")



        }




    }


}



