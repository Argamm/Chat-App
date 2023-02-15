package com.example.chatapp

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

private val NTP_OFFSET = 2208988800L
private val NTP_PORT = 123
private val NTP_PACKET_SIZE = 48
private val NTP_MODE_CLIENT = 3

class Message {
    var name: String = ""
    var textMessage: String = ""
    var imageUrl: String = "" // new property for storing the image URL
    private var messageTime: Long = 0

    constructor() {}
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(name: String, textMessage: String, imageUrl: String): this() {
        this.name = name
        this.textMessage = textMessage
        this.imageUrl = imageUrl
        val  sss = java.sql.Timestamp(System.currentTimeMillis())// for all users is the same, tested in two devices
        val pp = sss.time
        this.messageTime = pp
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