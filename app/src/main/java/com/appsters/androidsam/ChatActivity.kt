package com.appsters.androidsam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
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
import com.appsters.androidsam.data.ChatMessage
import com.appsters.androidsam.data.VoicePreset
import com.appsters.androidsam.ui.ChatAdapter
import com.appsters.androidsam.ui.ChatViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

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
    private var currentPreset: VoicePreset? = null

    private val recordAudioPermissionCode = 1
    private lateinit var speechRecognizer: SpeechRecognizer

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.setCurrentPreset(presetName)

        setupUIForPreset()
        setupRecyclerView()
        setupClickListeners()
        setupWebView()
        setupSpeechRecognizer()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClickListeners() {
        micButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioPermissionCode)
                    } else {
                        startRecognition()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stopRecognition()
                    true
                }
                else -> false
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

    @SuppressLint("SetJavaScriptEnabled")
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
        micButton.setImageResource(R.drawable.ic_mic_active) // Or some other visual feedback
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...")
        }
        speechRecognizer.startListening(intent)
    }

    private fun stopRecognition() {
        micButton.setImageResource(R.drawable.ic_mic) // Restore original icon
        speechRecognizer.stopListening()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Error from server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown speech recognition error"
                }
                Toast.makeText(this@ChatActivity, errorMessage, Toast.LENGTH_SHORT).show()
                micButton.setImageResource(R.drawable.ic_mic) // Restore original icon
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processAndSpeakText(matches[0])
                }
                micButton.setImageResource(R.drawable.ic_mic) // Restore original icon
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
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
        if (requestCode == recordAudioPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Don't start recognition here, it will be started on the next touch down
            } else {
                Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    @Suppress("unused")
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