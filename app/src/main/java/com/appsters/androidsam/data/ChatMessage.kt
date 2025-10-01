package com.appsters.androidsam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val presetName: String,
    val inputText: String,
    val outputText: String,
    val timestamp: Long,
    
    // Store voice parameters for custom preset
    val speed: Int,
    val pitch: Int,
    val throat: Int,
    val mouth: Int
)