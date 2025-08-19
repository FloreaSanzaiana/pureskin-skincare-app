import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication2.R
import com.example.myapplication2.model.Message
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class ChatAdapter(
    private val messages: MutableList<Message> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_WELCOME = 0
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
        private const val VIEW_TYPE_DATE_SEPARATOR = 3

        private const val DATE_SEPARATOR_MARKER = "###DATE_SEPARATOR###"

        private val URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
            Pattern.CASE_INSENSITIVE
        )
    }

    private fun makeLinksClickable(text: String, context: Context): SpannableString {
        val spannableString = SpannableString(text)
        val matcher = URL_PATTERN.matcher(text)

        while (matcher.find()) {
            val url = matcher.group(1)
            val start = matcher.start(1)
            val end = matcher.end(1)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrl(context, url)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = Color.BLUE
                    ds.isUnderlineText = true
                }
            }

            spannableString.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun containsUrls(text: String): Boolean {
        return URL_PATTERN.matcher(text).find()
    }

    private fun extractFirstUrl(text: String): String? {
        val matcher = URL_PATTERN.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    private fun getDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_WELCOME
        } else {
            val messagePosition = position - 1
            val message = messages[messagePosition]
            when {
                message.text.startsWith(DATE_SEPARATOR_MARKER) -> VIEW_TYPE_DATE_SEPARATOR
                message.isFromUser -> VIEW_TYPE_USER
                else -> VIEW_TYPE_BOT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_WELCOME -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_welcome_card, parent, false)
                WelcomeViewHolder(view)
            }
            VIEW_TYPE_DATE_SEPARATOR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_separator, parent, false)
                DateSeparatorViewHolder(view)
            }
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_BOT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_bot, parent, false)
                BotMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WelcomeViewHolder -> {
            }
            is DateSeparatorViewHolder -> {
                val messagePosition = position - 1
                holder.bind(messages[messagePosition])
            }
            is UserMessageViewHolder -> {
                val messagePosition = position - 1
                holder.bind(messages[messagePosition])
            }
            is BotMessageViewHolder -> {
                val messagePosition = position - 1
                holder.bind(messages[messagePosition])
            }
        }
    }

    override fun getItemCount(): Int = messages.size + 1

    fun addMessage(message: Message) {
        if (message.text.startsWith(DATE_SEPARATOR_MARKER)) {
            messages.add(message)
            notifyItemInserted(messages.size)
            return
        }

        if (shouldAddDateSeparator(message)) {
            val dateSeparator = createDateSeparator(message.timestamp)
            messages.add(dateSeparator)
            notifyItemInserted(messages.size)
        }

        messages.add(message)
        notifyItemInserted(messages.size)
    }

    private fun shouldAddDateSeparator(newMessage: Message): Boolean {
        if (messages.isEmpty()) return true

        val lastRealMessage = messages.lastOrNull { !it.text.startsWith(DATE_SEPARATOR_MARKER) }
        if (lastRealMessage == null) return true

        return !isSameDay(lastRealMessage.timestamp, newMessage.timestamp)
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun createDateSeparator(timestamp: Long): Message {
        val dateText = formatDateForSeparator(timestamp)
        return Message(
            text ="$DATE_SEPARATOR_MARKER$dateText",
            isFromUser = false,
            timestamp = timestamp
        )
    }

    private fun formatDateForSeparator(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return when {
            isSameDay(timestamp, today.timeInMillis) -> "Good day!"
            isSameDay(timestamp, yesterday.timeInMillis) -> "Yesterday"
            else -> {
                val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    class DateSeparatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.tv_date_separator)

        fun bind(message: Message) {
            val text = message.text.removePrefix(DATE_SEPARATOR_MARKER)
            dateText.text = text
        }
    }

    fun processExistingMessagesWithDateSeparators() {
        if (messages.isEmpty()) return

        val messagesWithSeparators = mutableListOf<Message>()
        var lastDate: String? = null

        for (message in messages) {
            if (message.text.startsWith(DATE_SEPARATOR_MARKER)) {
                messagesWithSeparators.add(message)
                continue
            }

            val currentDate = formatDateForSeparator(message.timestamp)

            if (lastDate != currentDate) {
                val separator = createDateSeparator(message.timestamp)
                messagesWithSeparators.add(separator)
                lastDate = currentDate
            }

            messagesWithSeparators.add(message)
        }

        messages.clear()
        messages.addAll(messagesWithSeparators)
        notifyDataSetChanged()
    }

    fun addMessages(newMessages: List<Message>) {
        for (message in newMessages) {
            addMessage(message)
        }
    }

    fun showTypingIndicator() {
        val typingMessage = Message(
            text = "",
            isFromUser = false,
            isTyping = true
        )
        addMessage(typingMessage)
    }

    fun hideTypingIndicator() {
        val oldSize = messages.size
        messages.removeAll { it.isTyping }
        val newSize = messages.size
        if (oldSize != newSize) {
            notifyItemRangeRemoved(newSize + 1, oldSize - newSize)
        }
    }

    fun clearMessages() {
        val oldSize = messages.size
        messages.clear()
        notifyItemRangeRemoved(1, oldSize)
    }

    class WelcomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.tv_message_time)
        private val messageStatus: ImageView = itemView.findViewById(R.id.iv_message_status)

        fun bind(message: Message) {
            if (containsUrls(message.text)) {
                val clickableText = makeLinksClickable(message.text, itemView.context)
                messageText.text = clickableText
                messageText.movementMethod = LinkMovementMethod.getInstance()
            } else {
                messageText.text = message.text
                messageText.movementMethod = null
            }

            messageTime.text = formatMessageTime(message.timestamp)
            messageStatus.visibility = View.VISIBLE
        }
    }

    inner class BotMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message_text)
        private val messageTime: TextView = itemView.findViewById(R.id.tv_message_time)
        private val typingIndicator: View = itemView.findViewById(R.id.typing_indicator)
        private val messageContainer: View = itemView.findViewById(R.id.message_container)
        private val linkPreview: TextView? = itemView.findViewById(R.id.tv_link_preview)

        private val typingDot1: View? = typingIndicator.findViewById(R.id.typing_dot_1)
        private val typingDot2: View? = typingIndicator.findViewById(R.id.typing_dot_2)
        private val typingDot3: View? = typingIndicator.findViewById(R.id.typing_dot_3)

        private val typingAnimators = mutableListOf<ObjectAnimator>()

        fun bind(message: Message) {
            if (message.isTyping) {
                messageContainer.visibility = View.GONE
                typingIndicator.visibility = View.VISIBLE
                linkPreview?.visibility = View.GONE

                startTypingAnimation()
            } else {
                messageContainer.visibility = View.VISIBLE
                typingIndicator.visibility = View.GONE

                stopTypingAnimation()

                if (containsUrls(message.text)) {
                    val clickableText = makeLinksClickable(message.text, itemView.context)
                    messageText.text = clickableText
                    messageText.movementMethod = LinkMovementMethod.getInstance()
                } else {
                    messageText.text = message.text
                    messageText.movementMethod = null
                }

                val url = extractFirstUrl(message.text)
                if (url != null && linkPreview != null) {
                    val domain = getDomain(url)
                    linkPreview.text = " $domain"
                    linkPreview.visibility = View.VISIBLE

                    linkPreview.setOnClickListener {
                        openUrl(itemView.context, url)
                    }
                } else {
                    linkPreview?.visibility = View.GONE
                }

                messageTime.text = formatMessageTime(message.timestamp)
            }
        }

        private fun startTypingAnimation() {
            stopTypingAnimation()

            val dots = listOfNotNull(typingDot1, typingDot2, typingDot3)

            dots.forEachIndexed { index, dot ->
                val animator = ObjectAnimator.ofFloat(dot, "translationY", 0f, -3f, 0f).apply {
                    duration = 600
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.RESTART
                    startDelay = (index * 150).toLong()

                    interpolator = android.view.animation.DecelerateInterpolator()
                }

                typingAnimators.add(animator)
                animator.start()
            }
        }

        private fun stopTypingAnimation() {
            typingAnimators.forEach { it.cancel() }
            typingAnimators.clear()

            listOfNotNull(typingDot1, typingDot2, typingDot3).forEach { dot ->
                dot.translationY = 0f
            }
        }
    }

    private fun formatMessageTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}