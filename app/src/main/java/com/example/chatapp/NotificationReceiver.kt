package com.example.chatapp

import android.R
import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat

class NotificationReceiver: BroadcastReceiver() {
    var notificationManager: NotificationManager? = null
    override fun onReceive(context: Context, intent: Intent) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (intent.hasExtra("ID")) {
            val noteId = intent.getIntExtra("ID", 0)
            notificationManager!!.cancel(noteId)
        } else {
            val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                val feedback = remoteInput.getCharSequence("DirectReplyNotification")
                val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.stat_notify_chat)
                    .setContentTitle("Thank you for your feedback!!!")
                notificationManager!!.notify(0, mBuilder.build())
            }
        }
    }
}