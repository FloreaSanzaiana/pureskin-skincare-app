package com.example.myapplication2.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication2.MainActivity
import com.example.myapplication2.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val routineType = intent.getStringExtra("routine_type") ?: "Routine"
        val message = intent.getStringExtra("message") ?: "Time for your skincare routine!"

        showNotification(context, routineType, message)
        val spfId = intent.getIntExtra("spf_id", -1)
        val routineId = intent.getIntExtra("routine_id", -1)
        val dayOfWeek = intent.getIntExtra("day_of_week", -1)
        val timeIndex = intent.getIntExtra("time_index", -1)

        if (spfId != -1) {
            rescheduleSpfForNextWeek(context, spfId, dayOfWeek, timeIndex)
        } else if (routineId != -1 && dayOfWeek != -1) {
            rescheduleForNextWeek(context, routineId, dayOfWeek, routineType)
        }

    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "skincare_routine"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Skincare Routines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for skincare routines"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getIconForRoutineType(title.lowercase()))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getIconForRoutineType(routineType: String): Int {
        return when {
            routineType.contains("morning") -> R.drawable.sun
            routineType.contains("evening") -> R.drawable.moon
            routineType.contains("exfoliation") -> R.drawable.exfoliation
            routineType.contains("face mask") -> R.drawable.face_mask_icon
            routineType.contains("eye mask") -> R.drawable.eye_mask_icon
            routineType.contains("lip mask") -> R.drawable.lip_mask
            routineType.contains("sunscreen") -> R.drawable.sunscreen_icon
            else -> android.R.drawable.stat_notify_chat
        }
    }
    private fun rescheduleSpfForNextWeek(context: Context, spfId: Int, dayOfWeek: Int, timeIndex: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val sharedPrefs = context.getSharedPreferences("skincare_prefs", Context.MODE_PRIVATE)
            val savedTime = sharedPrefs.getString("spf_${spfId}_time_$timeIndex", null) ?: return

            val timeParts = savedTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("routine_type", "SPF Reminder")
                putExtra("message", getSpfMessage(hour, minute))
                putExtra("spf_id", spfId)
                putExtra("day_of_week", dayOfWeek)
                putExtra("time_index", timeIndex)
                action = "SPF_NOTIFICATION_${spfId}_${dayOfWeek}_$timeIndex"
            }

            val requestCode = spfId * 1000 + dayOfWeek * 100 + timeIndex
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }

        } catch (e: Exception) {
            Log.e("RescheduleSpf", "Failed: ${e.message}")
        }
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

    private fun rescheduleForNextWeek(
        context: Context,
        routineId: Int,
        dayOfWeek: Int,
        routineType: String
    ) {
        try {
            Log.d("RescheduleAlarm", "Rescheduling alarm for next week...")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("RescheduleAlarm", "Cannot schedule exact alarms!")
                return
            }

            val sharedPrefs = context.getSharedPreferences("skincare_prefs", Context.MODE_PRIVATE)
            val savedTime = sharedPrefs.getString("routine_${routineId}_time", "08:00")
            val timeParts = savedTime?.split(":") ?: listOf("8", "0")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("routine_type", routineType.replaceFirstChar { it.uppercase() })
                putExtra("message", getRoutineMessage(routineType))
                putExtra("routine_id", routineId)
                putExtra("day_of_week", dayOfWeek)
                action = "SKINCARE_NOTIFICATION_${routineId}_$dayOfWeek"
            }

            val requestCode = routineId * 10 + dayOfWeek
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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

        } catch (e: Exception) {
            Log.e("RescheduleAlarm", "Failed to reschedule: ${e.message}")
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
}