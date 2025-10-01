package com.appsters.androidsam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE presetName = :presetName ORDER BY timestamp ASC")
    fun getMessagesForPreset(presetName: String): Flow<List<ChatMessage>>
    
    @Insert
    suspend fun insertMessage(message: ChatMessage)

    @Suppress("unused")
    @Query("DELETE FROM chat_messages WHERE presetName = :presetName")
    suspend fun deleteMessagesForPreset(presetName: String)

    @Suppress("unused")
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}