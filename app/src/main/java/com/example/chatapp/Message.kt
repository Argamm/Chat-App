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

private fun getNtpTime(): Long {
    try {
        val buffer = ByteArray(NTP_PACKET_SIZE)
        val request = DatagramPacket(buffer, buffer.size, InetAddress.getByName("pool.ntp.org"), NTP_PORT)

        val socket = DatagramSocket()
        socket.soTimeout = 1000
        socket.send(request)

        val response = DatagramPacket(buffer, buffer.size)
        socket.receive(response)
        socket.close()

        val received = response.data
        val secsSince1900 = (received[40].toLong() and 0xff shl 24) or (received[41].toLong() and 0xff shl 16) or (received[42].toLong() and 0xff shl 8) or (received[43].toLong() and 0xff)
        val secsSince1970 = secsSince1900 - NTP_OFFSET
        return secsSince1970 * 1000
    } catch (e: Exception) {
        // Handle the exception
        return 0
    }
}


class Message {
    var name: String = ""
    var textMessage: String = ""
    private var messageTime: Long = 0

    constructor() {}
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(name: String, textMessage: String): this() {
        this.name = name
        this.textMessage = textMessage
//        this.messageTime = Date().time//TODO  time for all users is not the same
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentTime = calendar.timeInMillis
//        val a = DateTimeFormatter
//            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
//            .withZone(ZoneOffset.UTC)
//            .format(Instant.now())

//        timeInMillis = calendar.getTimeInMillis();
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