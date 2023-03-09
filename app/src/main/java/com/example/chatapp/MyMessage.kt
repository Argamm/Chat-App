package com.example.chatapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*
import java.util.*

private val NTP_OFFSET = 2208988800L
private val NTP_PORT = 123
private val NTP_PACKET_SIZE = 48
private val NTP_MODE_CLIENT = 3

class MyMessage {
    var id: Int = 0
    var senderId: String = "" // new property for storing senderId
    var name: String = ""
    var textMessage: String = ""
    var imageUrl: String = "" // new property for storing the image URL
    private var messageTime: Long = 0

    constructor() {}

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(id: Int, senderId: String, name: String, textMessage: String, imageUrl: String) : this() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentTime = calendar.timeInMillis

        this.senderId = senderId
        this.name = name
        this.textMessage = textMessage
        this.imageUrl = imageUrl
        this.messageTime = currentTime
        this.id = id
    }

    fun getUsername(): String {
        return name
    }

    fun setUsername(userName: String) {
        this.name = userName
    }

    fun getTextsMessage(): String {
        return textMessage
    }

    fun setTextsMessage(textMessage: String) {
        this.textMessage = textMessage
    }

    fun getMessageTime(): Long {
        return this.messageTime
    }

    fun setMessageTime(messageTime: Long) {
        this.messageTime = messageTime
    }
}