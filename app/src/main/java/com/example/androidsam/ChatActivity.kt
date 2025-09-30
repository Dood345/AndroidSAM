package com.example.androidsam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidsam.data.ChatMessage
import com.example.androidsam.data.VoicePreset
import com.example.androidsam.speech.VoskRecognitionManager
import com.example.androidsam.ui.ChatAdapter
import com.example.androidsam.ui.ChatViewModel
import kotlinx.coroutines.launch
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var customSliders: LinearLayout
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var micButton: ImageButton
    private lateinit var sendButton: Button
    private lateinit var textInput: EditText
    private lateinit var webView: WebView

    private lateinit var pitchSeekBar: SeekBar
    private lateinit var speedSeekBar: SeekBar
    private lateinit var mouthSeekBar: SeekBar
    private lateinit var throatSeekBar: SeekBar

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var viewModel: ChatViewModel
    private lateinit var voskRecognitionManager: VoskRecognitionManager

    private var currentPreset: VoicePreset? = null

    private val RECORD_AUDIO_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)

        val presetName = intent.getStringExtra("PRESET_NAME")
        if (presetName == null) {
            Toast.makeText(this, "Preset not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentPreset = VoicePreset.getPreset(presetName)
        title = "$presetName Chat"

        // Initialize Views
        customSliders = findViewById(R.id.customSliders)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        micButton = findViewById(R.id.micButton)
        sendButton = findViewById(R.id.sendButton)
        textInput = findViewById(R.id.textInput)
        webView = findViewById(R.id.webView)
        pitchSeekBar = findViewById(R.id.pitchSeekBar)
        speedSeekBar = findViewById(R.id.speedSeekBar)
        mouthSeekBar = findViewById(R.id.mouthSeekBar)
        throatSeekBar = findViewById(R.id.throatSeekBar)

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        viewModel.setCurrentPreset(presetName)
        voskRecognitionManager = VoskRecognitionManager(this)

        setupUIForPreset()
        setupRecyclerView()
        setupClickListeners()
        setupWebView()

        observeMessages()
    }

    private fun setupUIForPreset() {
        if (currentPreset?.name == "Custom") {
            customSliders.visibility = View.VISIBLE
            pitchSeekBar.progress = currentPreset!!.pitch
            speedSeekBar.progress = currentPreset!!.speed
            mouthSeekBar.progress = currentPreset!!.mouth
            throatSeekBar.progress = currentPreset!!.throat
        } else {
            customSliders.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { message ->
            replayMessage(message)
        }
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun setupClickListeners() {
        micButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
            } else {
                startRecognition()
            }
        }

        sendButton.setOnClickListener {
            val inputText = textInput.text.toString().trim()
            if (inputText.isNotEmpty()) {
                processAndSpeakText(inputText)
                textInput.text.clear()
            }
        }
    }
    
    private fun processAndSpeakText(text: String) {
        val preset = if (currentPreset?.name == "Custom") {
            VoicePreset(
                "Custom",
                speedSeekBar.progress,
                pitchSeekBar.progress,
                throatSeekBar.progress,
                mouthSeekBar.progress
            )
        } else {
            currentPreset!!
        }

        val message = ChatMessage(
            presetName = preset.name,
            inputText = text,
            outputText = text,
            timestamp = Date().time,
            speed = preset.speed,
            pitch = preset.pitch,
            throat = preset.throat,
            mouth = preset.mouth
        )
        viewModel.saveMessage(message)
        speakText(message.outputText, message.pitch, message.speed, message.mouth, message.throat)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            mediaPlaybackRequiresUserGesture = false
        }
        webView.addJavascriptInterface(SAMInterface(), "Android")
        webView.loadUrl("file:///android_asset/sam.html")
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                chatAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    chatRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun startRecognition() {
        Toast.makeText(this, "Speech recognition not implemented yet.", Toast.LENGTH_SHORT).show()
    }

    private fun replayMessage(message: ChatMessage) {
        speakText(message.outputText, message.pitch, message.speed, message.mouth, message.throat)
    }

    private fun speakText(text: String, pitch: Int, speed: Int, mouth: Int, throat: Int) {
        val cleanedText = text.replace("\n", " ").replace("'", "\\'").replace("\"", "\\\"")
        val script = "javascript:speakText('$cleanedText', $pitch, $speed, $mouth, $throat)"
        webView.evaluateJavascript(script, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecognition()
            } else {
                Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class SAMInterface {
        @JavascriptInterface
        fun onSpeechComplete() {}

        @JavascriptInterface
        fun onSpeechError(error: String) {
            runOnUiThread {
                Toast.makeText(this@ChatActivity, "SAM Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}