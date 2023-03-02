package com.example.chatapp

import android.R
import android.app.*
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray


const val CHANNEL_ID = "notification_channel"
const val channelName = "com.example.chatapp"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    var notificationManager: NotificationManager? = null
    var notification: Notification? = null

    var defaultSoundUri: Uri? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage!!)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (remoteMessage != null) {
            /*   //message without data payload
              val title = remoteMessage.getNotification()?.getTitle();
            val message = remoteMessage.getNotification()?.getBody();
            notifyUser(title!!, message!!)
*/
            // Check if message contains a data payload.
            var dataMap: Map<String?, String?> = HashMap()
            var noteType: String? = ""
            if (remoteMessage.data.size > 0) {
                noteType = remoteMessage.data["type"]
                dataMap = remoteMessage.data
            }
            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            when (noteType) {
                "BIGTEXT" -> bigTextNotification(dataMap)
                "BIGPIC" -> bigPicNotification(dataMap)
                "ACTIONS" -> notificationActions(dataMap)
                "DIRECTREPLY" -> directReply(dataMap)
                "INBOX" -> inboxTypeNotification(dataMap)
                "MESSAGE" -> messageTypeNotification(dataMap)
            }
        }
    }

    private fun notifyUser(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            FLAG_ONE_SHOT
        )
        val channelId = "CHANNEL_ID"
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setColor(Color.BLUE)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // For android Oreo and above  notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fcm notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }


    fun bigTextNotification(dataMap: Map<String?, String?>) {
        val title = dataMap["title"]
        val message = dataMap["message"]
        val channelId = "CHANNEL_ID"
        val channelName = "FCMPush"
        val builder1 = NotificationCompat.Builder(this, channelId)
        if (Build.VERSION.SDK_INT >= 26) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager!!.createNotificationChannel(chan)
        }
        val style = NotificationCompat.BigTextStyle()
        style.bigText(message)
        style.setSummaryText(title)
        builder1.setContentTitle(title)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setSmallIcon(R.drawable.stat_notify_chat)
            .setColor(Color.BLUE)
            .setStyle(style)
        builder1.build()
        notification = builder1.notification
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(10, notification)
        } else {
            notificationManager!!.notify(0, notification)
        }
    }

    fun bigPicNotification(dataMap: Map<String?, String?>) {
        val title = dataMap["title"]
        val message = dataMap["message"]
        val imageUrl = dataMap["imageUrl"]
        try {
            val channelId = "CHANNEL_ID"
            val channelName = "FCMPush"
            val builder2 = NotificationCompat.Builder(this, channelId)
            if (Build.VERSION.SDK_INT >= 26) {
                val chan = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(chan)
            }
            val style = NotificationCompat.BigPictureStyle()
            style.setBigContentTitle(title)
            style.setSummaryText(message)
            style.bigPicture(
                Glide.with(this).asBitmap().load(imageUrl).submit().get()
            )
            builder2.setContentTitle(title)
                .setContentText(message)
                .setSound(defaultSoundUri)
                .setColor(Color.GREEN)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.stat_notify_chat)
                .setStyle(style)
            builder2.build()
            notification = builder2.notification
            if (Build.VERSION.SDK_INT >= 26) {
                startForeground(10, notification)
            } else {
                notificationManager!!.notify(0, notification)
            }
        } catch (e: Exception) {
        }
    }

    fun notificationActions(dataMap: Map<String?, String?>) {
        val title = dataMap["title"]
        val message = dataMap["message"]
        val channelId = "CHANNEL_ID"
        val channelName = "FCMPush"
        val builder3 = NotificationCompat.Builder(this, channelId)
        if (Build.VERSION.SDK_INT >= 26) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager!!.createNotificationChannel(chan)
        }
        val intent1 = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent1,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val cancelIntent = Intent(baseContext, NotificationReceiver::class.java)
        cancelIntent.putExtra("ID", 0)
        val cancelPendingIntent = PendingIntent.getBroadcast(baseContext, 0, cancelIntent, 0)
        builder3.setSmallIcon(R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(Color.BLUE)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_menu_view, "VIEW", pendingIntent)
            .addAction(R.drawable.ic_delete, "DISMISS", cancelPendingIntent)
            .build()
        notification = builder3.notification
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(10, notification)
        } else {
            notificationManager!!.notify(0, notification)
        }
    }

    fun directReply(dataMap: Map<String?, String?>) {
        val title = dataMap["title"]
        val message = dataMap["message"]
        val channelId = "CHANNEL_ID"
        val channelName = "FCMPush"
        val builder4 = NotificationCompat.Builder(this, channelId)
        if (Build.VERSION.SDK_INT >= 26) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager!!.createNotificationChannel(chan)
        }
        val cancelIntent = Intent(baseContext, NotificationReceiver::class.java)
        cancelIntent.putExtra("ID", 0)
        val cancelPendingIntent = PendingIntent.getBroadcast(baseContext, 0, cancelIntent, 0)
        val feedbackIntent = Intent(this, NotificationReceiver::class.java)
        val feedbackPendingIntent = PendingIntent.getBroadcast(
            this,
            100, feedbackIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remoteInput: androidx.core.app.RemoteInput? = androidx.core.app.RemoteInput.Builder("DirectReplyNotification")
            .setLabel(message)
            .build()
        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_delete,
            "Write here...", feedbackPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
        builder4.setSmallIcon(R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(feedbackPendingIntent)
            .addAction(action)
            .setColor(Color.RED)
            .addAction(R.drawable.ic_menu_compass, "Cancel", cancelPendingIntent)
        builder4.build()
        notification = builder4.notification
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(10, notification)
        } else {
            notificationManager!!.notify(0, notification)
        }
    }

    fun inboxTypeNotification(dataMap: Map<String?, String?>) {
        try {
            val title = dataMap["title"]
            val message = dataMap["message"]
            val jsonArray = JSONArray(dataMap["contentList"])
            val channelId = "CHANNEL_ID"
            val channelName = "FCMPush"
            val builder5 = NotificationCompat.Builder(this, channelId)
            if (Build.VERSION.SDK_INT >= 26) {
                val chan = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager!!.createNotificationChannel(chan)
            }
            val style = NotificationCompat.InboxStyle()
            style.setSummaryText(message)
            style.setBigContentTitle(title)
            for (i in 0 until jsonArray.length()) {
                val emailName = jsonArray.getString(i)
                style.addLine(emailName)
            }
            builder5.setContentTitle(title)
                .setContentText(message)
                .setColor(Color.BLUE)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setSmallIcon(R.drawable.stat_notify_chat)
                .setStyle(style)
            builder5.build()
            notification = builder5.notification
            if (Build.VERSION.SDK_INT >= 26) {
                startForeground(10, notification)
            } else {
                notificationManager!!.notify(0, notification)
            }
        } catch (e: Exception) {
        }
    }

    fun messageTypeNotification(dataMap: Map<String?, String?>?) {
        val channelId = "CHANNEL_ID"
        val channelName = "FCMPush"
        val builder6 = NotificationCompat.Builder(this, channelId)
        if (Build.VERSION.SDK_INT >= 26) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager!!.createNotificationChannel(chan)
        }
        val style = NotificationCompat.MessagingStyle("Janhavi")
        style.addMessage("Is there any online tutorial for FCM?", 0, "member1")
        style.addMessage("Yes", 0, "")
        style.addMessage("How to use constraint layout?", 0, "member2")
        builder6.setSmallIcon(R.drawable.stat_notify_chat)
            .setColor(Color.RED)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.stat_notify_chat))
            .setSound(defaultSoundUri)
            .setStyle(style)
            .setAutoCancel(true)
        builder6.build()
        notification = builder6.notification
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(10, notification)
        } else {
            notificationManager!!.notify(0, notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token!!)
    }

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        sendMessage(remoteMessage)
//    }
//
//    fun sendMessage(remoteMessage: RemoteMessage) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//            action = Intent.ACTION_MAIN
//            addCategory(Intent.CATEGORY_LAUNCHER)
//        }
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = Random.nextInt()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel(notificationManager)
//        }
//
////        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            100,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE and PendingIntent.FLAG_CANCEL_CURRENT and PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(remoteMessage.notification?.title)
//            .setContentText(remoteMessage.notification?.body)
//            .setSmallIcon(android.R.drawable.stat_notify_chat)
//            .setAutoCancel(true)
//            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
//            .setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.notification?.body))
//            .setContentIntent(pendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(notificationId, notification)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager) {
//        val channelName = "channelName"
//        val channel = NotificationChannel(
//            CHANNEL_ID,
//            channelName,
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "My channel description"
//            enableLights(true)
//            lightColor = Color.GREEN
//        }
//        channel.enableVibration(true)
//        notificationManager.createNotificationChannel(channel)
//    }
}
