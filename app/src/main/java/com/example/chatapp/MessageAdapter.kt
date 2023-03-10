package com.example.chatapp

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    messageList: RecyclerView,
    options: FirebaseRecyclerOptions<MyMessage>
) : FirebaseRecyclerAdapter<MyMessage, MessageAdapter.MessageViewHolder>(options) {
    val messageList = messageList

    // Keep track of the previous month and day
    private var prevMonthAndDay: String? = null

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var messageUser = itemView.findViewById<TextView>(R.id.messageUser)
        private var messageText = itemView.findViewById<TextView>(R.id.messageText)
        var messageTime = itemView.findViewById<TextView>(R.id.messageTime)
        private var insideMessageTime = itemView.findViewById<TextView>(R.id.insideMessageTime)
        private var messageImage = itemView.findViewById<AppCompatImageView>(R.id.messageImage)

        fun bind(message: MyMessage?) {
            message?.let {
                messageUser.text = message.getUsername()
                messageText.text = it.getTextsMessage()
                insideMessageTime.text = getTime(it.getMessageTime())

                val attacher = PhotoViewAttacher(messageImage)
                attacher.isZoomable = true
                attacher.maximumScale = 10f

                if (it.imageUrl.isNotEmpty()) { // If imageUrl is not empty
                    messageImage.visibility = View.VISIBLE
                    messageText.visibility = View.GONE
                    messageImage.load(it.imageUrl) {
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

                val monthAndDay = getMonthAndDay(it.getMessageTime())
                if (monthAndDay != prevMonthAndDay) {
                    prevMonthAndDay = monthAndDay

                    messageTime.visibility = View.VISIBLE
                    messageTime.text = prevMonthAndDay
                } else {
                    messageTime.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: MyMessage) {
        holder.bind(model)
        val currentMessage = getItem(position)
        val prevMessage = if (position > 0) getItem(position - 1) else null
        val currentMonthAndDay = getMonthAndDay(currentMessage.getMessageTime())
        val prevMonthAndDay = prevMessage?.let { getMonthAndDay(it.getMessageTime()) }

        if (currentMonthAndDay != prevMonthAndDay) {
            holder.messageTime.visibility = View.VISIBLE
            holder.messageTime.text = currentMonthAndDay
        } else {
            holder.messageTime.visibility = View.GONE
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        val layoutManager = messageList.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
        if (lastVisibleItemPosition == -1 || lastVisibleItemPosition == itemCount - 2) {
            messageList.scrollToPosition(itemCount - 1)
        } else {
            messageList.scrollToPosition(lastVisibleItemPosition)
        }
    }

    override fun getItem(position: Int): MyMessage {
        return super.getItem(position)
    }

    override fun getItemCount() = super.getItemCount()

    fun getMonthAndDay(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd") // specify month and day format
        val formatedDate = formatter.format(Date(timestamp))
        return formatedDate
    }

    fun getTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("hh:mm:ss a") // specify time format
        val formatedDate = formatter.format(Date(timestamp))
        return formatedDate
    }
}