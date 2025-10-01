package com.appsters.androidsam

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsters.androidsam.data.VoicePreset
import com.appsters.androidsam.ui.PresetAdapter

class PresetSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset_selection)

        val presetsRecyclerView: RecyclerView = findViewById(R.id.presetsRecyclerView)
        presetsRecyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = PresetAdapter(VoicePreset.PRESETS) { selectedPreset ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("PRESET_NAME", selectedPreset.name)
            }
            startActivity(intent)
        }
        presetsRecyclerView.adapter = adapter

        val settingsButton: ImageButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}