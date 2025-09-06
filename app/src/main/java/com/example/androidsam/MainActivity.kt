package com.example.androidsam

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var speakButton: Button
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val JAVASCRIPT_LOG_TAG = "SAM_JS_LOG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editText = findViewById(R.id.editText)
        speakButton = findViewById(R.id.speakButton)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        // Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Required for AudioContext in some cases
        webView.settings.allowFileAccess = true // Allow loading local sam.html

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(JAVASCRIPT_LOG_TAG, "WebView onPageStarted: $url")
                progressBar.visibility = View.VISIBLE // Show progress bar when page starts loading
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(JAVASCRIPT_LOG_TAG, "WebView onPageFinished: $url")
                progressBar.visibility = View.GONE // Hide progress bar when page finishes loading
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = View.GONE
                val errorMessage = "WebView Error: ${error?.description} for URL: ${request?.url}"
                Log.e(JAVASCRIPT_LOG_TAG, errorMessage)
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        // Add JavaScript interface
        webView.addJavascriptInterface(SAMInterface(), "Android")

        // Load the HTML file from assets
        webView.loadUrl("file:///android_asset/sam.html")

        speakButton.setOnClickListener {
            val textToSpeak = editText.text.toString().trim()
            if (textToSpeak.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                Log.d(JAVASCRIPT_LOG_TAG, "Speak button clicked with text: $textToSpeak")
                // Basic text cleaning (replace newlines and escape quotes for JS)
                val cleanedText = textToSpeak.replace("\n", " ").replace("'", "\\'").replace("\"", "\\\"")
                val script = "javascript:speakText('$cleanedText')"
                Log.d(JAVASCRIPT_LOG_TAG, "Evaluating script: $script")
                webView.evaluateJavascript(script, null)
            } else {
                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class SAMInterface {
        @JavascriptInterface
        fun onSpeechComplete() {
            Log.d(JAVASCRIPT_LOG_TAG, "JavaScript onSpeechComplete called")
            runOnUiThread {
                progressBar.visibility = View.GONE
                Toast.makeText(applicationContext, "Speech complete", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun logMessage(message: String) {
            Log.d(JAVASCRIPT_LOG_TAG, "JS: $message")
        }

        @JavascriptInterface
        fun logError(errorMessage: String) {
            Log.e(JAVASCRIPT_LOG_TAG, "JS ERROR: $errorMessage")
            runOnUiThread {
                progressBar.visibility = View.GONE // Stop spinner on JS error
                Toast.makeText(applicationContext, "JavaScript Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
}