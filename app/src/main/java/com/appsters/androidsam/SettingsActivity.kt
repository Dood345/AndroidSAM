package com.appsters.androidsam

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsters.androidsam.data.VoicePreset
import com.appsters.androidsam.ui.ChatViewModel
import com.appsters.androidsam.ui.PresetSettingsAdapter

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        val clearAllButton: Button = findViewById(R.id.clearAllButton)
        clearAllButton.setOnClickListener {
            viewModel.clearAllMessages()
            Toast.makeText(this, "All messages cleared", Toast.LENGTH_SHORT).show()
        }

        val presetsRecyclerView: RecyclerView = findViewById(R.id.presetsRecyclerView)
        presetsRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PresetSettingsAdapter(VoicePreset.PRESETS) { presetName ->
            viewModel.clearMessagesForPreset(presetName)
            Toast.makeText(this, "Messages for $presetName cleared", Toast.LENGTH_SHORT).show()
        }
        presetsRecyclerView.adapter = adapter
    }
}