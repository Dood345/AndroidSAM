package com.appsters.androidsam.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appsters.androidsam.R
import com.appsters.androidsam.data.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val onReplayClick: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, ChatAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view, onReplayClick)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        itemView: View,
        private val onReplayClick: (ChatMessage) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val timestampText: TextView = itemView.findViewById(R.id.timestampText)
        private val presetInfoText: TextView = itemView.findViewById(R.id.presetInfoText)
        private val inputText: TextView = itemView.findViewById(R.id.inputText)
        private val outputText: TextView = itemView.findViewById(R.id.outputText)
        private val replayButton: Button = itemView.findViewById(R.id.replayButton)

        fun bind(message: ChatMessage) {
            timestampText.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
            if (message.presetName == "Custom") {
                presetInfoText.visibility = View.VISIBLE
                presetInfoText.text = itemView.context.getString(
                    R.string.preset_info_format,
                    message.pitch,
                    message.speed,
                    message.mouth,
                    message.throat
                )
            } else {
                presetInfoText.visibility = View.GONE
            }
            inputText.text = message.inputText
            outputText.text = message.outputText
            replayButton.setOnClickListener { onReplayClick(message) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}