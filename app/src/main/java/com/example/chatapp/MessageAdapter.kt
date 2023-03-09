package com.example.chatapp

import android.app.Application
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.google.firebase.database.*
import java.text.SimpleDateFormat

class MessageAdapter(
    messageList: RecyclerView,
    options: FirebaseRecyclerOptions<MyMessage>
) : FirebaseRecyclerAdapter<MyMessage, MessageAdapter.MessageViewHolder>(options) {
    val messageList = messageList

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                messageText.visibility = View.GONE
                messageImage.load(message.imageUrl) {
                    crossfade(false)
                    crossfade(500)
                    placeholder(R.drawable.img)
                    //transformations(CircleCropTransformation())
                }
                messageImage.visibility = View.VISIBLE // Show the ImageView
            } else {
                messageText.visibility = View.VISIBLE
                messageImage.visibility = View.GONE // Hide the ImageView
            }
        }

        fun getDateFormat(timestamp: Long): String {
            val formatter = SimpleDateFormat.getDateTimeInstance() //or use getDateInstance()
            val formatedDate = formatter.format(timestamp)
            return formatedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return MessageViewHolder(view)
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: MyMessage) {
        holder.bind(model)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        messageList.scrollToPosition(itemCount - 1)
    }

    override fun getItem(position: Int): MyMessage {
        return super.getItem(position)
    }

    override fun getItemCount() = super.getItemCount()
}