package com.appsters.androidsam.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appsters.androidsam.R
import com.appsters.androidsam.data.VoicePreset

class PresetSettingsAdapter(
    private val presets: List<VoicePreset>,
    private val onClearPreset: (String) -> Unit
) : RecyclerView.Adapter<PresetSettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preset_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preset = presets[position]
        holder.presetNameTextView.text = preset.name
        holder.clearPresetButton.setOnClickListener {
            onClearPreset(preset.name)
        }
    }

    override fun getItemCount() = presets.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val presetNameTextView: TextView = view.findViewById(R.id.presetNameTextView)
        val clearPresetButton: Button = view.findViewById(R.id.clearPresetButton)
    }
}