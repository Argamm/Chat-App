package com.example.chatapp

import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var messageUser = itemView.findViewById<TextView>(R.id.messageUser)
    private var messageText = itemView.findViewById<TextView>(R.id.messageText)
    private var messageTime = itemView.findViewById<TextView>(R.id.messageTime)
    fun bind(message: Message) {
        var name: String = ""
        if (message.getUsername().length >= 12)
            name = message.getUsername().toString().substringBefore('@')
        else
            name = message.getUsername()

        messageUser.text = name
        messageText.text = message.getTextsMessage()
        messageTime.text = getDateFormat(message.getMessageTime())
    }

    fun getDateFormat(timestamp: Long) : String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
        return formatter.format(date)
    }
}