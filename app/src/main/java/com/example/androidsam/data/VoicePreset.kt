package com.example.androidsam.data

data class VoicePreset(
    val name: String,
    val speed: Int,
    val pitch: Int,
    val throat: Int,
    val mouth: Int
) {
    companion object {
        val PRESETS = listOf(
            VoicePreset("SAM", 72, 64, 128, 128),
            VoicePreset("Elf", 72, 64, 110, 160),
            VoicePreset("Little Robot", 92, 60, 190, 190),
            VoicePreset("Stuffy Guy", 82, 72, 110, 105),
            VoicePreset("Little Old Lady", 82, 32, 145, 145),
            VoicePreset("Extra-Terrestrial", 100, 64, 150, 200),
            VoicePreset("Custom", 72, 64, 128, 128) // Default values for custom
        )
        
        fun getPreset(name: String): VoicePreset {
            return PRESETS.find { it.name == name } ?: PRESETS[0]
        }
    }
}