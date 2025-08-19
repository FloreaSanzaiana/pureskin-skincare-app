package com.example.myapplication2.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.data.model.SpfRoutine
import java.util.*

class NotificationHelper(private val context: Context) {


    fun scheduleRoutineNotifications(routine: Routine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val selectedDays = parseSelectedDays(routine.notify_days)

        val timeParts = routine.notify_time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        selectedDays.forEach { dayOfWeek ->
            scheduleWeeklyAlarm(alarmManager, routine, dayOfWeek, hour, minute)
        }

        Log.d("NotificationHelper", "Scheduled notifications for ${routine.routine_type}")
    }

    private fun parseSelectedDays(notifyDays: String): List<Int> {

        val cleanString = notifyDays.replace("{", "").replace("}", "").replace("'", "")
        val dayNames = cleanString.split(",").map { it.trim().lowercase() }

        val dayMap = mapOf(
            "sunday" to Calendar.SUNDAY,
            "monday" to Calendar.MONDAY,
            "tuesday" to Calendar.TUESDAY,
            "wednesday" to Calendar.WEDNESDAY,
            "thursday" to Calendar.THURSDAY,
            "friday" to Calendar.FRIDAY,
            "saturday" to Calendar.SATURDAY
        )

        return dayNames.mapNotNull { dayMap[it] }
    }

    private fun scheduleWeeklyAlarm(
        alarmManager: AlarmManager,
        routine: Routine,
        dayOfWeek: Int,
        hour: Int,
        minute: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("routine_type", routine.routine_type.replaceFirstChar { it.uppercase() })
            putExtra("message", getRoutineMessage(routine.routine_type))
            putExtra("routine_id", routine.routine_id)
            putExtra("day_of_week", dayOfWeek)
            action = "SKINCARE_NOTIFICATION_${routine.routine_id}_$dayOfWeek"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.routine_id * 10 + dayOfWeek,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("NotificationHelper", "Used setExactAndAllowWhileIdle for ${routine.routine_type}")
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
                Log.d("NotificationHelper", "Used setRepeating for ${routine.routine_type}")
            }

            saveRoutineTime(routine.routine_id, "$hour:$minute")

            Log.d("NotificationHelper", "Scheduled alarm for ${routine.routine_type} on day $dayOfWeek at $hour:$minute")

        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to schedule alarm: ${e.message}")
        }
    }

    private fun getRoutineMessage(routineType: String): String {
        return when (routineType) {
            "morning" -> "Start your day with your morning skincare routine! ✨"
            "evening" -> "Time to wind down with your evening routine 🌙"
            "exfoliation" -> "Give your skin some love with exfoliation ✨"
            "face mask" -> "Pamper yourself with a face mask 💆‍♀️"
            "eye mask" -> "Treat your eyes to some care 👀"
            "lip mask" -> "Don't forget your lips! 💋"
            else -> "Time for your skincare routine!"
        }
    }

    fun cancelRoutineNotifications(routineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (dayOfWeek in 1..7) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "SKINCARE_NOTIFICATION_${routineId}_$dayOfWeek"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                routineId * 10 + dayOfWeek,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        Log.d("NotificationHelper", "Cancelled notifications for routine $routineId")
    }

    private fun saveRoutineTime(routineId: Int, time: String) {
        val sharedPrefs = context.getSharedPreferences("skincare_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("routine_${routineId}_time", time)
            .apply()
    }


    fun scheduleSpfNotifications(spfRoutine: SpfRoutine) {
        Log.d("NotificationHelper", "Scheduling SPF notifications...")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("NotificationHelper", "Cannot schedule exact alarms for SPF")
            return
        }

        val selectedDays = parseSpfActiveDays(spfRoutine.active_days)

        val startTimeParts = spfRoutine.start_time.split(":")
        val startHour = startTimeParts[0].toInt()
        val startMinute = startTimeParts[1].toInt()

        val endTimeParts = spfRoutine.end_time.split(":")
        val endHour = endTimeParts[0].toInt()
        val endMinute = endTimeParts[1].toInt()

        val intervalMinutes = spfRoutine.interval_minutes

        val notificationTimes = calculateSpfNotificationTimes(
            startHour, startMinute,
            endHour, endMinute,
            intervalMinutes
        )

        Log.d("NotificationHelper", "SPF notification times: $notificationTimes")

        selectedDays.forEach { dayOfWeek ->
            notificationTimes.forEachIndexed { index, time ->
                scheduleSpfAlarm(
                    alarmManager,
                    spfRoutine,
                    dayOfWeek,
                    time.first,
                    time.second,
                    index
                )
            }
        }

        Log.d("NotificationHelper", "Scheduled ${notificationTimes.size * selectedDays.size} SPF notifications")
    }

    private fun parseSpfActiveDays(activeDays: String): List<Int> {
        val dayNames = activeDays.split(",").map { it.trim().lowercase() }

        val dayMap = mapOf(
            "sunday" to Calendar.SUNDAY,
            "monday" to Calendar.MONDAY,
            "tuesday" to Calendar.TUESDAY,
            "wednesday" to Calendar.WEDNESDAY,
            "thursday" to Calendar.THURSDAY,
            "friday" to Calendar.FRIDAY,
            "saturday" to Calendar.SATURDAY
        )

        val result = dayNames.mapNotNull { dayMap[it] }
        Log.d("ParseSpfDays", "Parsed SPF days '$activeDays' to: $result")
        return result
    }

    private fun calculateSpfNotificationTimes(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int,
        intervalMinutes: Int
    ): List<Pair<Int, Int>> {

        val times = mutableListOf<Pair<Int, Int>>()

        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        var currentMinutes = startTotalMinutes

        while (currentMinutes <= endTotalMinutes) {
            val hour = currentMinutes / 60
            val minute = currentMinutes % 60

            if (hour <= 23) {
                times.add(Pair(hour, minute))
            }

            currentMinutes += intervalMinutes
        }

        Log.d("SpfTimes", "Calculated times from $startHour:$startMinute to $endHour:$endMinute every $intervalMinutes min: $times")
        return times
    }

    private fun scheduleSpfAlarm(
        alarmManager: AlarmManager,
        spfRoutine: SpfRoutine,
        dayOfWeek: Int,
        hour: Int,
        minute: Int,
        timeIndex: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("routine_type", "SPF Reminder")
            putExtra("message", getSpfMessage(hour, minute))
            putExtra("spf_id", spfRoutine.id)
            putExtra("day_of_week", dayOfWeek)
            putExtra("time_index", timeIndex)
            action = "SPF_NOTIFICATION_${spfRoutine.id}_${dayOfWeek}_$timeIndex"
        }

        val requestCode = spfRoutine.id * 1000 + dayOfWeek * 100 + timeIndex

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }

            saveSpfTime(spfRoutine.id, timeIndex, "$hour:$minute")

            Log.d("NotificationHelper", "Scheduled SPF alarm for day $dayOfWeek at $hour:$minute (requestCode: $requestCode)")

        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to schedule SPF alarm: ${e.message}")
        }
    }

    private fun saveSpfTime(spfId: Int, timeIndex: Int, time: String) {
        val sharedPrefs = context.getSharedPreferences("skincare_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("spf_${spfId}_time_$timeIndex", time)
            .apply()
    }

    private fun getSpfMessage(hour: Int, minute: Int): String {
        return when {
            hour < 10 -> "☀️ Good morning! Time to apply your SPF protection!"
            hour < 12 -> "🌤️ Mid-morning SPF reminder - keep your skin protected!"
            hour < 15 -> "☀️ Afternoon SPF reapplication time!"
            hour < 18 -> "🌅 Don't forget your SPF - still daylight hours!"
            else -> "🌆 Evening SPF reminder for continued protection!"
        }
    }

    fun cancelSpfNotifications(spfId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (dayOfWeek in 1..7) {
            for (timeIndex in 0..1000) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = "SPF_NOTIFICATION_${spfId}_${dayOfWeek}_$timeIndex"
                }
                val requestCode = spfId * 1000 + dayOfWeek * 100 + timeIndex
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }

        Log.d("NotificationHelper", "Cancelled SPF notifications for SPF $spfId")
    }




}