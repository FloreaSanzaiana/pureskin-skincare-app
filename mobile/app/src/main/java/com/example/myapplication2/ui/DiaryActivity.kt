package com.example.myapplication2.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DailyLogContent
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.data.model.RoutineCompletionDetails
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DiaryActivity: AppCompatActivity() {

    private lateinit var timelineButton: MaterialButton
    private lateinit var insightsButton: MaterialButton

    private lateinit var timelineView: ScrollView
    private lateinit var insightsView: ScrollView

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var completedCountText: TextView
    private lateinit var routinesRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout


    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var routinesAdapter: CompletedRoutinesAdapter
    private val completedRoutines = mutableListOf<RoutineCompletionDetails>()

    private var selectedDate: Date = Date()
    private var currentView = ViewType.TIMELINE
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var dailySummaryCard: CardView
    private lateinit var skinFeelingEmoji: TextView
    private lateinit var skinFeelingText: TextView
    private lateinit var stressEmoji: TextView
    private lateinit var stressText: TextView
    private lateinit var weatherEmoji: TextView
    private lateinit var weatherText: TextView
    private lateinit var skinConditionEmoji: TextView
    private lateinit var skinConditionText: TextView
    private lateinit var notesSection: LinearLayout
    private lateinit var notesText: TextView


    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var currentMonthText: TextView
    private lateinit var monthSubtitleText: TextView
    private lateinit var completionRateValue: TextView
    private lateinit var currentStreakValue: TextView
    private lateinit var skinHealthChart: LinearLayout
    private lateinit var routineCompletionChart: LinearLayout
    private lateinit var stressImpactChart: LinearLayout
    private lateinit var weatherImpactChart: LinearLayout

    private var currentInsightsMonth = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())


    enum class ViewType {
        TIMELINE, INSIGHTS
    }

    enum class PeriodType {
        WEEK, MONTH, YEAR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.diary_page)

        initViews()
        setupBottomNavigation()
        setupClickListeners()
        setupRecyclerView()
        setupCalendar()
        setupInsights()

        showTimelineView()
        loadRoutinesForDate(selectedDate)
        updateSelectedDateDisplay()
    }

    private fun initViews() {
        timelineButton = findViewById(R.id.timelineButton)
        insightsButton = findViewById(R.id.insightsButton)

        timelineView = findViewById(R.id.timelineView)
        insightsView = findViewById(R.id.insightsView)

        calendarView = findViewById(R.id.calendarView)
        selectedDateText = findViewById(R.id.selectedDateText)
        completedCountText = findViewById(R.id.completedCountText)
        routinesRecyclerView = findViewById(R.id.routinesRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)



        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        currentMonthText = findViewById(R.id.currentMonthText)
        monthSubtitleText = findViewById(R.id.monthSubtitleText)
        completionRateValue = findViewById(R.id.completionRateValue)
        currentStreakValue = findViewById(R.id.currentStreakValue)
        skinHealthChart = findViewById(R.id.skinHealthChart)
        routineCompletionChart = findViewById(R.id.routineCompletionChart)
        stressImpactChart = findViewById(R.id.stressImpactChart)
        weatherImpactChart = findViewById(R.id.weatherImpactChart)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        dailySummaryCard = findViewById(R.id.dailySummaryCard)
        skinFeelingEmoji = findViewById(R.id.skinFeelingEmoji)
        skinFeelingText = findViewById(R.id.skinFeelingText)
        stressEmoji = findViewById(R.id.stressEmoji)
        stressText = findViewById(R.id.stressText)
        weatherEmoji = findViewById(R.id.weatherEmoji)
        weatherText = findViewById(R.id.weatherText)
        skinConditionEmoji = findViewById(R.id.skinConditionEmoji)
        skinConditionText = findViewById(R.id.skinConditionText)
        notesSection = findViewById(R.id.notesSection)
        notesText = findViewById(R.id.notesText)
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.selectedItemId = R.id.nav_diary

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    val intent = Intent(this, MainMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
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
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        timelineButton.setOnClickListener {
            showTimelineView()
        }

        insightsButton.setOnClickListener {
            showInsightsView()
        }


        prevMonthButton.setOnClickListener {
            currentInsightsMonth.add(Calendar.MONTH, -1)
            updateInsightsData()
        }

        nextMonthButton.setOnClickListener {
            currentInsightsMonth.add(Calendar.MONTH, 1)
            updateInsightsData()
        }
    }

    private fun setupCalendar() {
        calendarView.date = System.currentTimeMillis()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time

            updateSelectedDateDisplay()
            loadRoutinesForDate(selectedDate)
        }
    }

    private fun setupRecyclerView() {
        routinesAdapter = CompletedRoutinesAdapter(completedRoutines) { routine ->
            showRoutineDetails(routine)
        }

        routinesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiaryActivity).apply {
                isAutoMeasureEnabled = true
            }
            adapter = routinesAdapter

            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER

            setHasFixedSize(false)

            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    requestLayout()
                }
            })
        }
    }

    private fun setupInsights() {
        updateInsightsData()
    }

    private fun showTimelineView() {
        currentView = ViewType.TIMELINE

        updateTabButtons(timelineActive = true)

        animateViewTransition(showTimeline = true)
    }

    private fun showInsightsView() {
        currentView = ViewType.INSIGHTS

        updateTabButtons(timelineActive = false)

        animateViewTransition(showTimeline = false)
        updateInsightsData()
    }

    private fun updateTabButtons(timelineActive: Boolean) {
        if (timelineActive) {
            timelineButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.white)
            )
            timelineButton.setTextColor(
                ContextCompat.getColor(this, R.color.primary_purple)
            )

            insightsButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.white_transparent)
            )
            insightsButton.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
        } else {
            insightsButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, android.R.color.white)
            )
            insightsButton.setTextColor(
                ContextCompat.getColor(this, R.color.primary_purple)
            )

            timelineButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.white_transparent)
            )
            timelineButton.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
        }
    }

    private fun animateViewTransition(showTimeline: Boolean) {
        if (showTimeline) {
            if (insightsView.visibility == View.VISIBLE) {
                val fadeOut = ObjectAnimator.ofFloat(insightsView, "alpha", 1f, 0f)
                fadeOut.duration = 150
                fadeOut.start()

                fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        insightsView.visibility = View.GONE
                        timelineView.visibility = View.VISIBLE

                        val fadeIn = ObjectAnimator.ofFloat(timelineView, "alpha", 0f, 1f)
                        fadeIn.duration = 150
                        fadeIn.start()
                    }
                })
            } else {
                timelineView.visibility = View.VISIBLE
                insightsView.visibility = View.GONE
            }
        } else {
            if (timelineView.visibility == View.VISIBLE) {
                val fadeOut = ObjectAnimator.ofFloat(timelineView, "alpha", 1f, 0f)
                fadeOut.duration = 150
                fadeOut.start()

                fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        timelineView.visibility = View.GONE
                        insightsView.visibility = View.VISIBLE

                        val fadeIn = ObjectAnimator.ofFloat(insightsView, "alpha", 0f, 1f)
                        fadeIn.duration = 150
                        fadeIn.start()
                    }
                })
            } else {
                insightsView.visibility = View.VISIBLE
                timelineView.visibility = View.GONE
            }
        }
    }

    private fun updateSelectedDateDisplay() {
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance()
        selected.time = selectedDate

        val displayText = when {
            isSameDay(today, selected) -> "Today, ${dateFormat.format(selectedDate)}"
            isYesterday(today, selected) -> "Yesterday, ${dateFormat.format(selectedDate)}"
            else -> dateFormat.format(selectedDate)
        }

        selectedDateText.text = displayText
    }

    private fun loadRoutinesForDate(date: Date) {
        val dateKey = dateKeyFormat.format(date)
        val mockData = getMockRoutinesForDate(dateKey)

        Log.d("DiaryActivity", "Loading for date: $dateKey, found: ${mockData.size} routines")

        runOnUiThread {
            completedRoutines.clear()
            completedRoutines.addAll(mockData)

            if (::routinesAdapter.isInitialized) {
                routinesAdapter.notifyDataSetChanged()
                Log.d("DiaryActivity", "Adapter notified, total items: ${routinesAdapter.itemCount}")
            }
            calculateRecyclerViewHeight()
            updateCompletedCount()
            updateEmptyState()
            loadDailyLogForDate(date)
        }

    }

    private fun updateCompletedCount() {
        val count = completedRoutines.size
        completedCountText.text = if (count == 0) "No routines" else "$count completed"

        val color = when {
            count == 0 -> ContextCompat.getColor(this, R.color.text_secondary)
            count < 3 -> ContextCompat.getColor(this, R.color.warning_orange)
            else -> ContextCompat.getColor(this, R.color.success_green)
        }
        completedCountText.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun updateEmptyState() {
        if (completedRoutines.isEmpty()) {
            routinesRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            routinesRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
    }



    private fun showRoutineDetails(routine: RoutineCompletionDetails) {
        Toast.makeText(this, "Clicked: ${routine.routine_type}", Toast.LENGTH_SHORT).show()
    }

    private fun getMockRoutinesForDate(dateKey: String): List<RoutineCompletionDetails> {
        Log.d("details",dateKey)
        val db= DatabaseManager(this)
        var completed_routines=db.getRoutineCompletionDetailsByDate(dateKey)
        if(completed_routines!=null) Log.d("details",completed_routines.toString())
        return completed_routines

        }


    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, selected: Calendar): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, selected)
    }
    private fun calculateRecyclerViewHeight() {
        routinesRecyclerView.post {
            val itemHeight = 150
            val totalHeight = completedRoutines.size * (itemHeight * resources.displayMetrics.density).toInt()

            val layoutParams = routinesRecyclerView.layoutParams
            layoutParams.height = totalHeight
            routinesRecyclerView.layoutParams = layoutParams
        }
    }


    private fun loadDailyLogForDate(date: Date) {
        val dateKey = dateKeyFormat.format(date)
        val db = DatabaseManager(this)

        try {

            val dailyLogList = db.getDailyLogContentByDate(dateKey)
            Log.d("details",dateKey)
            val dailyLog = dailyLogList

            displayDailyLog(dailyLog)

        } catch (e: Exception) {
            Log.e("DiaryActivity", "Error loading daily log: ${e.message}")
            dailySummaryCard.visibility = View.GONE
        }
    }

    private fun displayDailyLog(dailyLog: DailyLogContent?) {
        if (dailyLog != null) {
            dailySummaryCard.visibility = View.VISIBLE

            skinFeelingEmoji.text = getSkinFeelingEmoji(dailyLog.skin_feeling_score)
            skinFeelingText.text = getSkinFeelingText(dailyLog.skin_feeling_score)

            stressEmoji.text = getStressEmoji(dailyLog.stress_level)
            stressText.text = getStressText(dailyLog.stress_level)

            weatherEmoji.text = getWeatherEmoji(dailyLog.weather)
            weatherText.text = dailyLog.weather?.replaceFirstChar { it.uppercaseChar() } ?: "Unknown"

            skinConditionEmoji.text = getSkinConditionEmoji(dailyLog.skin_condition)
            skinConditionText.text = dailyLog.skin_condition?.replaceFirstChar { it.uppercaseChar() } ?: "Unknown"

            if (!dailyLog.notes.isNullOrEmpty()) {
                notesSection.visibility = View.VISIBLE
                notesText.text = dailyLog.notes
            } else {
                notesSection.visibility = View.GONE
            }

        } else {
            dailySummaryCard.visibility = View.GONE
        }
    }

    private fun getSkinFeelingEmoji(score: Int?): String {
        return when (score) {
            3 -> "😢"
            6 -> "😐"
            10->"🥰"
            else -> "😐"
        }
    }

    private fun getSkinFeelingText(score: Int?): String {
        return when (score) {
            3 -> "Bad"
            6 -> "Normal"
            10 -> "Good"
            else -> "Unknown"
        }
    }

    private fun getStressEmoji(level: Int?): String {
        return when (level) {
            3 -> "😰"
            6 -> "😐"
            10 -> "😌"
            else -> "😐"
        }
    }

    private fun getStressText(level: Int?): String {
        return when (level) {
            3 -> "Stressed"
            6 -> "Normal"
            10 -> "Calm"
            else -> "Unknown"
        }
    }

    private fun getWeatherEmoji(weather: String?): String {
        return when (weather?.lowercase()) {
            "sunny" -> "☀️"
            "cloudy" -> "☁️"
            "precipitations" -> "☔"
            else -> "🌤️"
        }
    }

    private fun getSkinConditionEmoji(condition: String?): String {
        return when (condition?.lowercase()) {
            "normal" -> "👌"
            "oily" -> "🐟"
            "dehydrated" -> "🌵"
            "itchy" -> "😖"
            else -> "👌"
        }
    }


    private fun updateInsightsData() {
        val db = DatabaseManager(this)

        updateMonthDisplay()

        updateKPICards(db)

        createSkinHealthChart(db)
        createRoutineCompletionChart(db)
        createStressImpactChart(db)
        createWeatherImpactChart(db)
    }

    private fun updateMonthDisplay() {
        currentMonthText.text = monthFormat.format(currentInsightsMonth.time)

        val db = DatabaseManager(this)
        val cal = Calendar.getInstance()
        cal.time = currentInsightsMonth.time

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = dateKeyFormat.format(cal.time)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = dateKeyFormat.format(cal.time)

        try {
            val routineCompletions = db.getRoutineCompletionsByDateRange(startDate, endDate)
            val daysWithRoutines = routineCompletions.map { it.completion_date }.distinct().size
            monthSubtitleText.text = "$daysWithRoutines days with routines"
        } catch (e: Exception) {
            monthSubtitleText.text = "0 days with routines"
            Log.e("DiaryActivity", "Error counting routine days: ${e.message}")
        }
    }

    private fun updateKPICards(db: DatabaseManager) {
        try {
            val cal = Calendar.getInstance()
            cal.time = currentInsightsMonth.time
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = dateKeyFormat.format(cal.time)

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = dateKeyFormat.format(cal.time)
            val totalDaysInMonth = cal.get(Calendar.DAY_OF_MONTH)

            val routineCompletions = db.getRoutineCompletionsByDateRange(startDate, endDate)
            val completedDays = routineCompletions.map { it.completion_date }.distinct().size
            val completionRate = (completedDays * 100) / totalDaysInMonth

            val streak = calculateCurrentStreak(db)

            completionRateValue.text = "✅ $completionRate%"
            currentStreakValue.text = "🔥 $streak"

        } catch (e: Exception) {
            Log.e("DiaryActivity", "Error updating KPI cards: ${e.message}")
            completionRateValue.text = "✅ --"
            currentStreakValue.text = "🔥 --"
        }
    }
    private fun calculateCurrentStreak2(db: DatabaseManager): Int {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        var streak = 0

        for (i in 0..100) {
            val dateKey = dateKeyFormat.format(cal.time)
            val routines = db.getRoutineCompletionDetailsByDate(dateKey)

            if (routines.isNotEmpty()) {
                streak++
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }

    private fun calculateCurrentStreak(db: DatabaseManager): Int {
        val cal = Calendar.getInstance()
        var streak = 0

        val todayKey = dateKeyFormat.format(cal.time)
        val todayRoutines = db.getRoutineCompletionDetailsByDate(todayKey)

        if (todayRoutines.isEmpty()) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        for (i in 0..100) {
            val dateKey = dateKeyFormat.format(cal.time)
            val routines = db.getRoutineCompletionDetailsByDate(dateKey)

            if (routines.isNotEmpty()) {
                streak++
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }

    private fun calculateCurrentStreak1(db: DatabaseManager): Int {
        val cal = Calendar.getInstance()
        var streak = 0

        for (i in 0..100) {
            val dateKey = dateKeyFormat.format(cal.time)
            val routines = db.getRoutineCompletionDetailsByDate(dateKey)

            if (routines.isNotEmpty()) {
                streak++
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }

    private fun createSkinHealthChart(db: DatabaseManager) {
        skinHealthChart.removeAllViews()

        try {
            val cal = Calendar.getInstance()
            cal.time = currentInsightsMonth.time

            val scores = mutableListOf<Float>()
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            Log.d("DiaryActivityy", "Days in month: $daysInMonth")

            for (week in 0..3) {
                val weekScores = mutableListOf<Int>()

                val startDay = week * 7 + 1
                val endDay = minOf((week + 1) * 7, daysInMonth)

                Log.d("DiaryActivityy", "Week $week: days $startDay to $endDay")

                for (dayOfMonth in startDay..endDay) {
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val dateKey = dateKeyFormat.format(cal.time)

                    val dailyLog = db.getDailyLogContentByDate(dateKey)
                    Log.d("DiaryActivityy", "Date $dateKey: ${dailyLog?.skin_feeling_score}")

                    dailyLog?.skin_feeling_score?.let {
                        weekScores.add(it)
                        Log.d("DiaryActivityy", "Added score: $it")
                    }
                }

                val avgScore = if (weekScores.isNotEmpty()) weekScores.average().toFloat() else 0f
                scores.add(avgScore)

                Log.d("DiaryActivityy", "Week $week avg: $avgScore (from ${weekScores.size} days)")
            }

            Log.d("DiaryActivityy", "Final scores: $scores")
            createLineChart(skinHealthChart, scores)

        } catch (e: Exception) {
            Log.e("DiaryActivityy", "Error creating skin health chart: ${e.message}")
            e.printStackTrace()
        }}

            private fun createRoutineCompletionChart(db: DatabaseManager) {
        routineCompletionChart.removeAllViews()

        try {
            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val completionData = mutableListOf<Int>()
            val absoluteCompletions = mutableListOf<Int>()

            val targetMonth = currentInsightsMonth.get(Calendar.MONTH)
            val targetYear = currentInsightsMonth.get(Calendar.YEAR)

            val calendarDays = listOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            )

            for (dayOfWeek in calendarDays) {
                var completions = 0
                var totalDays = 0

                val cal = Calendar.getInstance()
                cal.set(targetYear, targetMonth, 1)

                while (cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear) {
                    if (cal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                        totalDays++
                        val dateKey = dateKeyFormat.format(cal.time)
                        val routines = db.getRoutineCompletionDetailsByDate(dateKey)

                        if (routines.isNotEmpty()) completions++
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }

                absoluteCompletions.add(completions)
                Log.e("DiaryActivitytrends", "${daysOfWeek[calendarDays.indexOf(dayOfWeek)]}: $completions completions out of $totalDays days")
            }

            val maxCompletions = absoluteCompletions.maxOrNull() ?: 1
            Log.e("DiaryActivitytrends", "Max completions: $maxCompletions")

            for (completions in absoluteCompletions) {
                val relativeScore = if (maxCompletions > 0) {
                    (completions * 100) / maxCompletions
                } else 0
                completionData.add(relativeScore)
            }

            Log.e("DiaryActivitytrends", "Absolute completions: $absoluteCompletions")
            Log.e("DiaryActivitytrends", "Relative completion data: $completionData")

            createBarChart(routineCompletionChart, completionData, daysOfWeek)

        } catch (e: Exception) {
            Log.e("DiaryActivitytrends", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
    private fun createStressImpactChart(db: DatabaseManager) {
        stressImpactChart.removeAllViews()

        try {
            val stressLevels = listOf("😌", "😐", "😰")
            val avgScores = calculateAvgSkinScoreByStress(db)

            createImpactChart(stressImpactChart, avgScores, stressLevels,
                listOf(R.color.success_green, R.color.warning_orange, R.color.error_red))

        } catch (e: Exception) {
            Log.e("DiaryActivity", "Error creating stress impact chart: ${e.message}")
        }
    }

    private fun createWeatherImpactChart(db: DatabaseManager) {
        weatherImpactChart.removeAllViews()

        try {
            val weatherTypes = listOf("☀️", "☁️", "☔")
            val avgScores = calculateAvgSkinScoreByWeather(db)

            createImpactChart(weatherImpactChart, avgScores, weatherTypes,
                listOf(R.color.primary_purple, R.color.warning_orange, R.color.text_secondary))

        } catch (e: Exception) {
            Log.e("DiaryActivity", "Error creating weather impact chart: ${e.message}")
        }
    }

    private fun calculateAvgSkinScoreByStress(db: DatabaseManager): List<Float> {
        val stressRanges = listOf(1..3, 4..7, 8..10) // Low, Medium, High - CORECTAT
        val avgScores = mutableListOf<Float>()

        for (range in stressRanges) {
            val scores = mutableListOf<Int>()

            val cal = Calendar.getInstance()
            cal.time = currentInsightsMonth.time
            cal.set(Calendar.DAY_OF_MONTH, 1)

            while (cal.get(Calendar.MONTH) == currentInsightsMonth.get(Calendar.MONTH)) {
                val dateKey = dateKeyFormat.format(cal.time)
                val dailyLog = db.getDailyLogContentByDate(dateKey)

                if (dailyLog?.stress_level != null &&
                    dailyLog.stress_level in range &&
                    dailyLog.skin_feeling_score != null) {
                    scores.add(dailyLog.skin_feeling_score)
                }
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }

            val avgScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
            avgScores.add(avgScore)

            Log.d("StressChart", "Range $range: ${scores.size} entries, avg: $avgScore")
        }

        return avgScores
    }

    private fun calculateAvgSkinScoreByWeather(db: DatabaseManager): List<Float> {
        val weatherTypes = listOf("sunny", "cloudy", "precipitations")
        val avgScores = mutableListOf<Float>()

        for (weather in weatherTypes) {
            val scores = mutableListOf<Int>()

            val cal = Calendar.getInstance()
            cal.time = currentInsightsMonth.time
            cal.set(Calendar.DAY_OF_MONTH, 1)

            while (cal.get(Calendar.MONTH) == currentInsightsMonth.get(Calendar.MONTH)) {
                val dateKey = dateKeyFormat.format(cal.time)
                val dailyLog = db.getDailyLogContentByDate(dateKey)

                if (dailyLog?.weather?.lowercase() == weather && dailyLog.skin_feeling_score != null) {
                    scores.add(dailyLog.skin_feeling_score)
                }
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }

            val avgScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f
            avgScores.add(avgScore)

            Log.d("WeatherChart", "Weather $weather: ${scores.size} entries, avg: $avgScore")
        }

        return avgScores
    }



















    private fun createLineChart(container: LinearLayout, data: List<Float>) {
        container.removeAllViews()

        Log.d("LineChart", "Creating line chart with data: $data")

        if (data.all { it == 0f }) {
            val noDataText = TextView(this).apply {
                text = "No data available"
                textSize = 20f
                setTextColor(ContextCompat.getColor(this@DiaryActivity, R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                )
            }
            container.addView(noDataText)
            return
        }

        val maxValue = data.filter { it > 0 }.maxOrNull() ?: 10f
        val minValue = data.filter { it > 0 }.minOrNull() ?: 0f

        Log.d("LineChart", "Max: $maxValue, Min: $minValue")

        // Create custom line chart view
        val lineChartView = LineChartView(this, data, maxValue, minValue)
        lineChartView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            500 // Fixed height
        )

        container.addView(lineChartView)
    }
    // Custom View class pentru line chart

    private fun createBarChart(container: LinearLayout, data: List<Int>, labels: List<String>) {
        container.removeAllViews()

        val maxValue = data.maxOrNull() ?: 1
        val containerHeight = 550

        data.forEachIndexed { index, value ->
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, containerHeight).apply {
                    weight = 1f
                    marginEnd = if (index < data.size - 1) 16 else 0
                }
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }

            // Create bar
            val barHeight = if (maxValue > 0) {
                ((value.toFloat() / maxValue.toFloat()) * (containerHeight - 40)).toInt()
            } else 20

            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(60, barHeight.coerceAtLeast(4))
                background = ContextCompat.getDrawable(this@DiaryActivity, R.drawable.bar_gradient)
            }

            // Create label
            val label = TextView(this).apply {
                text = labels[index]
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@DiaryActivity, R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
            }

            barContainer.addView(bar)
            barContainer.addView(label)
            container.addView(barContainer)
        }
    }

    private fun createImpactChart(container: LinearLayout, data: List<Float>, icons: List<String>, colors: List<Int>) {
        container.removeAllViews()

        Log.d("ImpactChart", "Data: $data")

        // Calculează valoarea maximă din datele actuale, nu una fixă
        val maxValue = data.filter { it > 0 }.maxOrNull() ?: 1f
        val minValue = data.filter { it > 0 }.minOrNull() ?: 0f

        // Dacă toate valorile sunt 0, nu afișa nimic sau afișează mesaj
        if (maxValue == 0f) {
            // Poți adăuga aici un TextView cu mesaj "Nu există date suficiente"
            return
        }

        data.forEachIndexed { index, value ->
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    weight = 1f
                    if (index < data.size - 1) marginEnd = 32
                }
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }

            // Create icon
            val icon = TextView(this).apply {
                text = icons[index]
                textSize = 24f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            }

            // Calculează înălțimea proporțional cu valorile reale
            val barHeight = if (value > 0) {
                // Calculează proporția relativă la valoarea maximă
                val proportion = value / maxValue
                val maxBarHeight = 260 // Înălțime maximă pentru cea mai mare bară
                val minBarHeight = 10  // Înălțime minimă pentru barele cu valori

                // Calculează înălțimea: între minBarHeight și maxBarHeight
                (minBarHeight + (proportion * (maxBarHeight - minBarHeight))).toInt()
            } else {
                25 // Înălțime pentru barele fără date
            }

            val bar = TextView(this).apply {
                text = ""
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@DiaryActivity, android.R.color.white))
                gravity = android.view.Gravity.CENTER or android.view.Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(150, barHeight)
                background = ContextCompat.getDrawable(this@DiaryActivity, R.drawable.bar_gradient)
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@DiaryActivity, colors[index]))
                setPadding(0, 0, 0, 8)
            }

            barContainer.addView(icon)
            barContainer.addView(bar)
            container.addView(barContainer)
        }
    }
    class LineChartView(
        context: Context,
        private val data: List<Float>,
        private val maxValue: Float,
        private val minValue: Float
    ) : View(context) {

        private val linePaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.primary_purple)
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        private val dottedLinePaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.text_secondary)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }

        private val pointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.primary_purple)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        private val zeroPointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.text_secondary)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        private val textPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.text_secondary)
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (data.isEmpty()) return

            val padding = 80f
            val chartWidth = width - 2 * padding
            val chartHeight = height - 2 * padding

            // Calculate points
            val points = mutableListOf<Pair<Float, Float>>()

            data.forEachIndexed { index, value ->
                val x = padding + (index * chartWidth / (data.size - 1).coerceAtLeast(1))
                val y = if (value > 0 && maxValue > minValue) {
                    val normalizedValue = (value - minValue) / (maxValue - minValue)
                    padding + chartHeight - (normalizedValue * chartHeight)
                } else if (value > 0) {
                    padding + chartHeight / 2
                } else {
                    padding + chartHeight * 0.9f // Poziție jos pentru valori 0
                }
                points.add(Pair(x, y))
            }

            // Draw ALL lines between points
            for (i in 0 until points.size - 1) {
                val startPoint = points[i]
                val endPoint = points[i + 1]

                val hasDataStart = data[i] > 0
                val hasDataEnd = data[i + 1] > 0

                when {
                    hasDataStart && hasDataEnd -> {
                        // Linie solidă între două puncte cu date
                        canvas.drawLine(
                            startPoint.first, startPoint.second,
                            endPoint.first, endPoint.second,
                            linePaint
                        )
                    }
                    hasDataStart || hasDataEnd -> {
                        // Linie punctată între punct cu date și punct fără date
                        canvas.drawLine(
                            startPoint.first, startPoint.second,
                            endPoint.first, endPoint.second,
                            dottedLinePaint
                        )
                    }
                    else -> {
                        // Linie foarte subțire între două puncte fără date (baseline)
                        val baselinePaint = Paint().apply {
                            color = ContextCompat.getColor(context, R.color.text_secondary)
                            strokeWidth = 1f
                            style = Paint.Style.STROKE
                            alpha = 100
                        }
                        canvas.drawLine(
                            startPoint.first, startPoint.second,
                            endPoint.first, endPoint.second,
                            baselinePaint
                        )
                    }
                }
            }

            // Draw points and labels
            points.forEachIndexed { index, point ->
                val value = data[index]

                if (value > 0) {
                    // Draw data point
                    canvas.drawCircle(point.first, point.second, 12f, pointPaint)

                    // Draw value text above point
                    canvas.drawText(
                        String.format("%.1f", value),
                        point.first,
                        point.second - 30f,
                        textPaint
                    )
                } else {
                    // Draw empty point (smaller)
                    canvas.drawCircle(point.first, point.second, 6f, zeroPointPaint)

                    // Draw "0" text
                    canvas.drawText(
                        "0",
                        point.first,
                        point.second - 20f,
                        textPaint.apply { alpha = 150 }
                    )
                    textPaint.alpha = 255 // Reset alpha
                }

                // Draw week label below
                canvas.drawText(
                    "W${index + 1}",
                    point.first,
                    height - 30f,
                    textPaint
                )
            }
        }
    }

}

////////////