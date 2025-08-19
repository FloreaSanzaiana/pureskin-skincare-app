package com.example.myapplication2.ui

import ChatAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.data.model.DatabaseManager
import com.example.myapplication2.model.Message
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import android.widget.Toast
import com.example.myapplication2.data.model.UserMessage
import com.example.myapplication2.data.network.RetrofitInstance
import com.example.myapplication2.ui.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var inputArea: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var rootView: View
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isKeyboardOpen = false
    private var originalBottomMargin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.chat_page)

        initializeViews()
        setupRecyclerView()
        setupKeyboardListener()
        setupClickListeners()


        //addWelcomeMessage()
        getOldMessages()
        //loadAllMessages()
        // getOldMessages2()
        simpleScroll()

    }


    private fun initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        inputArea = findViewById(R.id.input_area)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)
        rootView = findViewById(android.R.id.content)
        messagesRecyclerView = findViewById(R.id.messages_recycler_view)

        val layoutParams = inputArea.layoutParams as CoordinatorLayout.LayoutParams
        originalBottomMargin = layoutParams.bottomMargin

        bottomNavigationView.selectedItemId = R.id.nav_chat
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        layoutManager = LinearLayoutManager(this)

        messagesRecyclerView.apply {
            this.layoutManager = this@ChatActivity.layoutManager
            adapter = chatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                }
            })
        }

    }

    private fun setupClickListeners() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> {
                    startActivity(Intent(this, MainMenuActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    true
                }
                R.id.nav_products -> {
                    startActivity(Intent(this, ProductsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    true
                }
                R.id.nav_chat -> true
                R.id.nav_recommendations -> {
                    startActivity(Intent(this, RecommendationsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    true
                }
                R.id.nav_diary -> {
                    startActivity(Intent(this, DiaryActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    })
                    true
                }
                else -> false
            }
        }

        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendUserMessage(message)
                messageInput.text.clear()
            }
        }
    }

    private fun addWelcomeMessage() {
        var text = "Hello! I'm your SkinSmart Assistant. How can I help you with your skincare today?"

        val welcomeMessage = Message(
            text=text,
            isFromUser = false
        )
        chatAdapter.addMessage(welcomeMessage)
        sendMessage(text)
    }

    private fun sendUserMessage(text: String) {
        val userMessage = Message(text = text, isFromUser = true, timestamp = convertTimestampToLong(getCurrentTimestamp()))
        chatAdapter.addMessage(userMessage)
        println(" Total items: ${chatAdapter.itemCount}")
        simpleScroll()
        //sendMessageAndGetResponse(text)
        sendMessageAndGetResponse2(text)
    }

    private fun botResponse(botMessage: Message) {
        chatAdapter.showTypingIndicator()
        println("Total items: ${chatAdapter.itemCount}")
        simpleScroll()

        messagesRecyclerView.postDelayed({
            chatAdapter.hideTypingIndicator()
            chatAdapter.addMessage(botMessage)
            println(" Bot message adăugat. Total items: ${chatAdapter.itemCount}")
            simpleScroll()
        }, 2000)
    }

    private fun generateBotResponse(): String {
        val responses = listOf(
            "That's a great question! For your specific skin concern, I recommend consulting with a dermatologist for personalized advice.",
            "Based on your query, here are some general skincare tips that might help. Remember to always patch test new products!",
            "I understand your concern. Skincare can be complex, but with the right approach, you can achieve healthy skin.",
            "That's an interesting question about skincare! Let me provide some helpful information for you.",
            "Thank you for your question. Here's what I can tell you about that skincare topic."
        )
        return responses.random()
    }

    private fun simpleScroll() {
        if (chatAdapter.itemCount == 0) {
            println("Nu pot face scroll - 0 items")
            return
        }

        val lastPosition = chatAdapter.itemCount - 1

        messagesRecyclerView.post {
            messagesRecyclerView.smoothScrollToPosition(lastPosition)
        }
    }

    fun addBotMessage(text: String) {
        val message = Message(text = text, isFromUser = false)
        chatAdapter.addMessage(message)
        simpleScroll()
    }

    fun clearChat() {
        chatAdapter.clearMessages()
        addWelcomeMessage()
    }

    private fun setupKeyboardListener() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.rootView.height
                val keypadHeight = screenHeight - rect.bottom
                val isKeyboardNowOpen = keypadHeight > screenHeight * 0.15

                if (isKeyboardNowOpen && !isKeyboardOpen) {
                    isKeyboardOpen = true
                    onKeyboardShown()
                } else if (!isKeyboardNowOpen && isKeyboardOpen) {
                    isKeyboardOpen = false
                    onKeyboardHidden()
                }
            }
        })
    }

    private fun onKeyboardShown() {
        bottomNavigationView.visibility = View.GONE

        val inputParams = inputArea.layoutParams as CoordinatorLayout.LayoutParams
        inputParams.bottomMargin = 0
        inputArea.layoutParams = inputParams

        val recyclerParams = messagesRecyclerView.layoutParams as CoordinatorLayout.LayoutParams
        recyclerParams.bottomMargin = dpToPx(16)
        messagesRecyclerView.layoutParams = recyclerParams

        messagesRecyclerView.postDelayed({
            simpleScroll()
        }, 100)
    }

    private fun onKeyboardHidden() {
        bottomNavigationView.visibility = View.VISIBLE
        bottomNavigationView.alpha = 0f
        bottomNavigationView.animate().alpha(1f).setDuration(150).start()

        val inputParams = inputArea.layoutParams as CoordinatorLayout.LayoutParams
        inputParams.bottomMargin = dpToPx(70)
        inputArea.layoutParams = inputParams

        val recyclerParams = messagesRecyclerView.layoutParams as CoordinatorLayout.LayoutParams
        recyclerParams.bottomMargin = dpToPx(130)
        messagesRecyclerView.layoutParams = recyclerParams
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onBackPressed() {
        if (isKeyboardOpen) {
            messageInput.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
        } else {
            super.onBackPressed()
        }
    }
    private  fun getOldMessages(){
        val db= DatabaseManager(this)
        val messages=db.getAllMessages()
        if(messages.size>0){

            for(m in messages){
                Log.d("message",m.text)
                var aux= Message(text=m.text, isFromUser =(m.sender=="user"), timestamp = convertTimestampToLong(m.timestamp))
                chatAdapter.addMessage(aux)
            }
        }
        else{
            addWelcomeMessage()
        }
    }
    fun convertTimestampToLong(timestampString: String): Long {
        return try {

            val timeRegex = Regex("""(\d{2}):(\d{2}):(\d{2})""")
            val match = timeRegex.find(timestampString)

            if (match != null) {
                val hour = match.groupValues[1].toInt()
                val minute = match.groupValues[2].toInt()
                val second = match.groupValues[3].toInt()


                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, second)
                calendar.set(Calendar.MILLISECOND, 0)

                val timestamp = calendar.timeInMillis

                val testFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                testFormat.timeZone = TimeZone.getDefault()
                val result = testFormat.format(Date(timestamp))

                timestamp

            } else {
                System.currentTimeMillis()
            }

        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName) {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            "Dec" -> 12
            else -> 1
        }
    }


    fun getCurrentTimestamp(): String {
        val currentTime = Date()
        val oneMinuteInMillis = 60 * 1000L
        val adjustedTime = Date(currentTime.time + oneMinuteInMillis)

        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getDefault()
        val timestamp = sdf.format(adjustedTime)

        Log.d("TimestampGen", " Timp original: '${sdf.format(currentTime)}'")
        Log.d("TimestampGen", "Generat LOCAL (+1 min): '$timestamp'")
        Log.d("TimestampGen", "Timezone folosit: ${TimeZone.getDefault().id}")

        return timestamp
    }



    private fun sendMessageAndGetResponse(message: String){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux=sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val currentTimestamp = getCurrentTimestamp()
        val mess= UserMessage(0,aux,"user",message,currentTimestamp)

        val call = apiService.send_message_and_get_response("Bearer $token",mess)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@ChatActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<UserMessage>> {
                override fun onResponse(call: Call<List<UserMessage>>, response: Response<List<UserMessage>>) {
                    if (response.isSuccessful) {
                        val messages = response.body()

                        if(messages!=null){
                            val db= DatabaseManager(this@ChatActivity)
                            db.insertMessage(messages[0])
                            db.insertMessage(messages[1])
                            val a= Message(text=messages[0].text, isFromUser = (messages[0].sender=="user"), timestamp = convertTimestampToLong(messages[0].timestamp))
                            val b= Message(text=messages[1].text, isFromUser = (messages[1].sender=="user"), timestamp = convertTimestampToLong(messages[1].timestamp))
                            botResponse(a)

                        }

                    } else {

                        Toast.makeText(this@ChatActivity, "Eroare la încărcarea produselor", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<UserMessage>>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    private fun sendMessageAndGetResponse2(message: String) {
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val currentTimestamp = getCurrentTimestamp()
        val mess = UserMessage(0, aux, "user", message, currentTimestamp)

        chatAdapter.showTypingIndicator()
        simpleScroll()
        println("Bot typing indicator - așteptăm răspunsul de la server...")

        val call = apiService.send_message_and_get_response("Bearer $token", mess)
        if (token.isNullOrEmpty()) {
            chatAdapter.hideTypingIndicator()
            Toast.makeText(this@ChatActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<List<UserMessage>> {
                override fun onResponse(call: Call<List<UserMessage>>, response: Response<List<UserMessage>>) {
                    chatAdapter.hideTypingIndicator()

                    if (response.isSuccessful) {
                        val messages = response.body()

                        if (messages != null) {
                            val db = DatabaseManager(this@ChatActivity)
                            db.insertMessage(messages[0])
                            db.insertMessage(messages[1])

                            val botMessage = Message(
                                text = messages[0].text,
                                isFromUser = (messages[0].sender == "user"),
                                timestamp = convertTimestampToLong(messages[0].timestamp)
                            )

                            chatAdapter.addMessage(botMessage)
                            simpleScroll()
                        }
                    } else {
                        Toast.makeText(this@ChatActivity, "Eroare la trimiterea mesajelor cu raspuns", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<UserMessage>>, t: Throwable) {
                    chatAdapter.hideTypingIndicator()
                    Toast.makeText(this@ChatActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    private fun sendMessage(message: String){
        val apiService = RetrofitInstance.instance
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("session_token", null)
        val aux = sharedPreferences.getString("user_id", null)?.toIntOrNull() ?: -1
        val currentTimestamp = getCurrentTimestamp()
        val mess = UserMessage(0, aux, "bot", message, currentTimestamp)
        val call = apiService.send_message("Bearer $token",mess)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@ChatActivity, "Session token expired.", Toast.LENGTH_SHORT).show()
        } else {
            call.enqueue(object : Callback<UserMessage> {
                override fun onResponse(call: Call<UserMessage>, response: Response<UserMessage>) {
                    if (response.isSuccessful) {
                        val messageReponsed=response.body()
                        if(messageReponsed!=null){
                            val db= DatabaseManager(this@ChatActivity)
                            db.insertMessage(messageReponsed)
                            Log.d("message","succes")

                        }

                    } else {

                        Toast.makeText(this@ChatActivity, "Eroare la trimiterea mesajului", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserMessage>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "Eroare de rețea", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun addDailyWelcomeMessage() {

        val sharedPreferences = getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)
        val lastWelcomeDate = sharedPreferences.getString("last_welcome_date", "")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastWelcomeDate != today) {
            val welcomeText = getRandomWelcomeMessage()
            sendMessage(welcomeText)
            val welcomeMessage = Message(
                text = welcomeText,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            chatAdapter.addMessage(welcomeMessage)

            sharedPreferences.edit()
                .putString("last_welcome_date", today)
                .apply()


        }
    }

    private fun getRandomWelcomeMessage(): String {
        val timeOfDay = getTimeOfDay()

        val welcomeMessages = resources.getStringArray(R.array.welcome_messages)
        val randomMessage = welcomeMessages.random()

        return if (randomMessage.contains("%1\$s")) {
            String.format(randomMessage, timeOfDay)
        } else {
            randomMessage
        }
    }

    private fun getTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> getString(R.string.time_morning)
            in 12..17 -> getString(R.string.time_afternoon)
            in 18..21 -> getString(R.string.time_evening)
            else -> getString(R.string.time_evening)
        }
    }

}