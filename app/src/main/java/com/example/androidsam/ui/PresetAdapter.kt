package com.example.androidsam.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidsam.R
import com.example.androidsam.data.VoicePreset

class PresetAdapter(
    private val presets: List<VoicePreset>,
    private val onPresetClick: (VoicePreset) -> Unit
) : RecyclerView.Adapter<PresetAdapter.PresetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preset, parent, false)
        return PresetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val preset = presets[position]
        holder.bind(preset)
        holder.itemView.setOnClickListener { onPresetClick(preset) }
    }

    override fun getItemCount(): Int = presets.size

    class PresetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.presetNameTextView)
        private val detailsTextView: TextView = itemView.findViewById(R.id.presetDetailsTextView)

        fun bind(preset: VoicePreset) {
            nameTextView.text = preset.name
            detailsTextView.text = itemView.context.getString(
                R.string.preset_details,
                preset.speed,
                preset.pitch,
                preset.mouth,
                preset.throat
            )
        }
    }
}