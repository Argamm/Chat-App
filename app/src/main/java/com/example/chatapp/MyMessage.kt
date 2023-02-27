package com.example.chatapp

import android.os.Build
import android.text.format.DateFormat.getDateFormat
import androidx.annotation.RequiresApi
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

private val NTP_OFFSET = 2208988800L
private val NTP_PORT = 123
private val NTP_PACKET_SIZE = 48
private val NTP_MODE_CLIENT = 3

class MyMessage {
    var name: String = ""
    var textMessage: String = ""
    var imageUrl: String = "" // new property for storing the image URL
    private var messageTime: Long = 0

    constructor() {}
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(name: String, textMessage: String, imageUrl: String): this() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentTime = calendar.timeInMillis

        this.name = name
        this.textMessage = textMessage
        this.imageUrl = imageUrl
        this.messageTime = currentTime
    }

    fun getUsername() : String{
        return name
    }

    fun setUsername(userName: String) {
        this.name = userName
    }

    fun getTextsMessage() : String{
        return textMessage
    }

    fun setTextsMessage(textMessage: String) {
        this.textMessage = textMessage
    }

    fun getMessageTime() : Long{
        return  this.messageTime
    }

    fun setMessageTime(messageTime: Long) {
        this.messageTime = messageTime
    }

}