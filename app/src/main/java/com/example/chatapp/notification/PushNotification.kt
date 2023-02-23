package com.example.chatapp.notification

data class PushNotification(
    var to: String,
    val notification: NotificationData,
)
