package com.example.chatapp

import android.os.SystemClock
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.days

class Message {
    var name: String = ""
    var textMessage: String = ""
    private var messageTime: Long = 0

    constructor() {}
    constructor(name: String, textMessage: String): this() {
        this.name = name
        this.textMessage = textMessage
        this.messageTime = Date().time//TODO  time for all users is not the same
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