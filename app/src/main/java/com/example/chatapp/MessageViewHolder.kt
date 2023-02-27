package com.example.chatapp

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.github.chrisbanes.photoview.PhotoViewAttacher
import java.text.SimpleDateFormat
import java.util.*

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var messageUser = itemView.findViewById<TextView>(R.id.messageUser)
    private var messageText = itemView.findViewById<TextView>(R.id.messageText)
    private var messageTime = itemView.findViewById<TextView>(R.id.messageTime)
    private var messageImage = itemView.findViewById<AppCompatImageView>(R.id.messageImage)

    fun bind(message: MyMessage) {
        var name: String = ""
        if (message.getUsername().length >= 12)
            name = message.getUsername().toString().substringBefore('@')
        else
            name = message.getUsername()

        messageUser.text = name
        messageText.text = message.getTextsMessage()
        messageTime.text = getDateFormat(message.getMessageTime())

        val attacher = PhotoViewAttacher(messageImage)
        attacher.isZoomable = true
        attacher.maximumScale = 10f

        if (message.imageUrl.isNotEmpty()) { // If imageUrl is not empty
            messageImage.visibility = View.VISIBLE
            messageImage.load(message.imageUrl) {
                crossfade(false)
                crossfade(500)
                placeholder(R.drawable.img)
//                transformations(CircleCropTransformation())
            }
            messageImage.visibility = View.VISIBLE // Show the ImageView
        } else {
            messageImage.visibility = View.GONE // Hide the ImageView
        }
    }

    fun getDateFormat(timestamp: Long) : String {
        val date = Calendar.getInstance().timeInMillis
        val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
        val formatedDate = formatter.format(date)

        return formatedDate
    }
}