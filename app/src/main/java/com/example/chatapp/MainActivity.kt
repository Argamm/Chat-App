package com.example.chatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.MetadataRepo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.databinding.ListItemBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseListAdapter

import com.firebase.ui.database.FirebaseListOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val signInCode: Int = 1
    private lateinit var activityMainContainer: ConstraintLayout
    private lateinit var firebaseListAdapter: FirebaseRecyclerAdapter<Message, MessageViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for sending emoji, but not using or tested
        EmojiCompat.init(BundledEmojiCompatConfig(this))


        activityMainContainer = binding.activityContainer

        //if user not autiroseited
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().build()
            startActivityForResult(signInIntent, signInCode)
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().build(),
                signInCode
            )
        } else {
//            Snackbar.make(activityMainContainer, "You are registered", Snackbar.LENGTH_LONG).show()

        }
        displayAllMessages()
        binding.btnSend.setOnClickListener {
            with(binding) {
                if (editMessageText.text.toString() != "") {
                    FirebaseDatabase.getInstance().getReference().push().setValue(
                        Message(
                            FirebaseAuth.getInstance().currentUser?.email ?: "",
                            editMessageText.text.toString()
                        )
                    )
//                    firebaseListAdapter.notifyDataSetChanged()
                    editMessageText.setText("")
                    displayAllMessages()
                }
            }
            it.hideKeyboard()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == signInCode) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activityMainContainer, "You are registered", Snackbar.LENGTH_LONG)
                    .show()
                displayAllMessages()
            } else {
                Snackbar.make(activityMainContainer, "You are not registered", Snackbar.LENGTH_LONG)
                    .show()
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun displayAllMessages() {
        val query = FirebaseDatabase.getInstance().getReference()
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java).setLifecycleOwner(this)
            .build()

        val messageList = binding.messageRecyclerView

        firebaseListAdapter =
            object : FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {

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
                    model: Message
                ) {
                    //for sending emoji, but not using or tested
                    holder.itemView.findViewById<TextView>(R.id.messageText).text =
                        EmojiCompat.get().process(model.textMessage)

                    holder.bind(model)
                }

                override fun onDataChanged() {
                    super.onDataChanged()
                    messageList.scrollToPosition(itemCount - 1)
                }
            }
        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = firebaseListAdapter
    }

    override fun onStart() {
        super.onStart()
        firebaseListAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        firebaseListAdapter.stopListening()
    }
}