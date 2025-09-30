package com.example.androidsam.speech

import android.content.Context
import android.media.AudioRecord
import org.vosk.Model
import org.vosk.Recognizer
import java.io.IOException

class VoskRecognitionManager(private val context: Context) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var recognitionThread: Thread? = null

    fun initialize(modelPath: String, onResult: (String) -> Unit, onPartialResult: (String) -> Unit) {
        try {
            model = Model(modelPath)
            recognizer = Recognizer(model, 16000.0f)
            val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
            val channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
            val bufferSize = AudioRecord.getMinBufferSize(16000, channelConfig, audioFormat)
            audioRecord = AudioRecord(
                android.media.MediaRecorder.AudioSource.MIC,
                16000,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: IOException) {
            // Handle model loading error
        }
    }

    fun startListening(onResult: (String) -> Unit, onPartialResult: (String) -> Unit) {
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord?.startRecording()
            recognitionThread = Thread {
                val buffer = ShortArray(4096)
                while (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val nread = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (nread > 0) {
                        if (recognizer?.acceptWaveForm(buffer, nread) == true) {
                            onResult(recognizer?.result ?: "")
                        } else {
                            onPartialResult(recognizer?.partialResult ?: "")
                        }
                    }
                }
            }
            recognitionThread?.start()
        }
    }

    fun stopListening() {
        audioRecord?.stop()
        recognitionThread?.interrupt()
        recognitionThread = null
    }

    fun destroy() {
        audioRecord?.release()
        model?.close()
    }
}