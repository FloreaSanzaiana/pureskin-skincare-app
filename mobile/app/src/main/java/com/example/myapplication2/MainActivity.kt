package com.example.myapplication2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication2.ui.ConcernsFlow
import com.example.myapplication2.ui.ForgotPasswordActivity
import com.example.myapplication2.ui.LoginActivity
import com.example.myapplication2.ui.NotificationReceiver
import com.example.myapplication2.ui.RegisterActivity
import com.example.myapplication2.ui.RegisterActivity2
import java.util.Calendar
import android.Manifest
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.myapplication2.data.model.Routine
import com.example.myapplication2.ui.NotificationHelper
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var textureView: TextureView
    private var mediaPlayer: MediaPlayer? = null
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNotificationPermissionGranted()
        } else {
            showNotificationPermissionDeniedDialog()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        checkNotificationPermissions()

        textureView = findViewById(R.id.textureView)

        val button: Button = findViewById<Button>(R.id.button1)

        button.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            //testNotificationReceiver()/////
            requestBatteryOptimizationExemption()
            //scheduleTestAlarm()/////

        }
        val button2: Button=findViewById<Button>(R.id.button2)

        button2.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

        val button3: Button=findViewById<Button>(R.id.button3)

        button3.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        val videoUri = Uri.parse("android.resource://${packageName}/raw/back2")

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MainActivity, videoUri)
            setSurface(android.view.Surface(surface))
            isLooping = true
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mediaPlayer?.release()
        mediaPlayer = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun testNotificationReceiver() {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("routine_type", "Test")
            putExtra("message", "Test notification - receiver works!")
            putExtra("routine_id", 999)
        }
        sendBroadcast(intent)
        Log.d("MainActivity", "Test broadcast sent")
    }
    private fun scheduleTestAlarm() {
        Log.d("TestAlarm", "Starting alarm test...")

        checkAlarmPermissions()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)

        val testTime = String.format("%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE))

        val dayOfWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "sunday"
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            else -> "monday"
        }

        Log.d("TestAlarm", "Current time: ${Calendar.getInstance().time}")
        Log.d("TestAlarm", "Scheduling for: ${calendar.time}")
        Log.d("TestAlarm", "Test time: $testTime on $dayOfWeek")

        val testRoutine = Routine(
            routine_id = 888,
            routine_type = "test",
            notify_time = testTime,
            notify_days = "{'$dayOfWeek'}", user_id = 0
        )

        val notificationHelper = NotificationHelper(this)
        notificationHelper.scheduleRoutineNotifications(testRoutine)

        Log.d("TestAlarm", "Test alarm scheduled! Should appear in ~1 minute")
    }

    private fun checkAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canScheduleExact = alarmManager.canScheduleExactAlarms()
            Log.d("AlarmPermissions", "Can schedule exact alarms: $canScheduleExact")

            if (!canScheduleExact) {
                Log.w("AlarmPermissions", "CANNOT SCHEDULE EXACT ALARMS!")
                requestExactAlarmPermission()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
            Log.d("AlarmPermissions", "Ignoring battery optimizations: $isIgnoringBatteryOptimizations")

            if (!isIgnoringBatteryOptimizations) {
                Log.w("AlarmPermissions", "App is subject to battery optimization!")
            }
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("AlarmPermissions", "Failed to open exact alarm settings: ${e.message}")
            }
        }
    }

    private fun scheduleTestAlarmDirect() {
        Log.d("DirectAlarmTest", "Testing direct AlarmManager...")

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("routine_type", "Direct Test")
            putExtra("message", "Direct AlarmManager test worked!")
            putExtra("routine_id", 777)
            action = "DIRECT_TEST_ALARM"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            12345,
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
                Log.d("DirectAlarmTest", "Direct alarm scheduled for: ${calendar.time}")
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("DirectAlarmTest", "Direct alarm (legacy) scheduled for: ${calendar.time}")
            }
        } catch (e: SecurityException) {
            Log.e("DirectAlarmTest", "Failed to schedule direct alarm: ${e.message}")
        } catch (e: Exception) {
            Log.e("DirectAlarmTest", "Unexpected error: ${e.message}")
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("BatteryOptimization", "Requesting battery optimization exemption...")

                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("BatteryOptimization", "Failed to open battery settings: ${e.message}")

                    // Fallback - deschide setările generale de baterie
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(fallbackIntent)
                }
            } else {
                Log.d("BatteryOptimization", "App is already exempt from battery optimization")
            }
        }
    }

    private fun checkNotificationPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        showNotificationPermissionRationaleDialog()
                    } else {
                        requestNotificationPermission()
                    }
                } else {
                    onNotificationPermissionGranted()
                }
            }
            else -> {
                if (!areNotificationsEnabled()) {
                    showNotificationSettingsDialog()
                } else {
                    onNotificationPermissionGranted()
                }
            }
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showNotificationPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("This app needs notification permission to remind you about your skincare routines.")
            .setPositiveButton("Allow") { _, _ ->
                requestNotificationPermission()
            }
            .setNegativeButton("Skip") { _, _ ->
                showNotificationPermissionDeniedDialog()
            }
            .setCancelable(false)
            .show()
    }

    private fun showNotificationPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notifications Disabled")
            .setMessage("You can enable notifications later in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppNotificationSettings()
            }
            .setNegativeButton("Continue") { _, _ -> }
            .show()
    }

    private fun showNotificationSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Please enable notifications for this app in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppNotificationSettings()
            }
            .setNegativeButton("Skip") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun openAppNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                }
            }
        }
        startActivity(intent)
    }

    private fun onNotificationPermissionGranted() {
        Log.d("NotificationPermissions", "Notifications enabled!")
    }

    fun ensureNotificationsEnabled(onSuccess: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                onSuccess()
            } else {
                checkNotificationPermissions()
            }
        } else {
            if (areNotificationsEnabled()) {
                onSuccess()
            } else {
                checkNotificationPermissions()
            }
        }
    }
}
