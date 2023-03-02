package com.example.chatapp

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*

class MessageAdapter(messageList: RecyclerView, options: FirebaseRecyclerOptions<MyMessage>) :
    FirebaseRecyclerAdapter<MyMessage, MessageViewHolder>(options) {
    val messageList = messageList
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int,
        model: MyMessage
    ) {
//        val message = getItem(position)
//
//        holder.itemView.setOnLongClickListener {
//            val builder = AlertDialog.Builder(it.context)
//            builder.setMessage("Are you sure you want to delete this message?")
//            builder.setPositiveButton("Delete") { dialog, _ ->
//                val ref = FirebaseDatabase.getInstance().getReference("messages")
//                val query: Query = ref.orderByChild("id").equalTo(message.id.toDouble())
//                query.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        for (snapshot in dataSnapshot.children) {
//                            snapshot.ref.removeValue()
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        Log.e("TAG_onCanceled", "onCancelled", databaseError.toException())
//                    }
//                })
//                dialog.dismiss()
//            }
//            builder.setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }
//            builder.show()
//            true // Return true to indicate that the event has been consumed
//        }
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