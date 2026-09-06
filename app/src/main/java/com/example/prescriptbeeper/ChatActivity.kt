package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF020505.toInt())
            fitsSystemWindows = true
        }

        root.addView(TextView(this).apply {
            text = "SPEAK WITH HERMES"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(24, (32 * resources.displayMetrics.density).toInt(), 24, 24)
        })

        messagesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 0, 24, 24)
        }

        scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        scrollView.addView(messagesContainer)
        root.addView(scrollView)

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 24)
        }

        val editText = EditText(this).apply {
            hint = "Type a message..."
            setTextColor(0xFFdff1ff.toInt())
            setHintTextColor(0xFF5c6975.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        inputRow.addView(editText)

        val sendButton = Button(this).apply { text = "Send" }
        inputRow.addView(sendButton)
        root.addView(inputRow)

        setContentView(root)

        addBubble("THE INDEX AWAITS YOUR WORDS, MANAGER.", isUser = false)

        sendButton.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addBubble(text, isUser = true)
            editText.text.clear()
            addBubble("...", isUser = false, isPlaceholder = true)

            thread {
                val reply = GeminiChatClient.sendMessage(text)
                runOnUiThread {
                    messagesContainer.removeViewAt(messagesContainer.childCount - 1)
                    addBubble(reply, isUser = false)
                }
            }
        }
    }

    private fun addBubble(text: String, isUser: Boolean, isPlaceholder: Boolean = false) {
        val bubble = TextView(this).apply {
            this.text = text
            setTextColor(if (isUser) 0xFFdff1ff.toInt() else 0xFF4be8ff.toInt())
            textSize = 13f
            setPadding(24, 20, 24, 20)
            setBackgroundColor(if (isUser) 0xFF10181c.toInt() else 0xFF15242a.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
                gravity = if (isUser) Gravity.END else Gravity.START
            }
            alpha = if (isPlaceholder) 0.5f else 1f
        }
        messagesContainer.addView(bubble)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}