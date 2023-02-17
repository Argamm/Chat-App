package com.example.chatapp

import android.Manifest.permission.*
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.storage.FirebaseStorage
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val signInCode: Int = 1
    private lateinit var activityMainContainer: ConstraintLayout
    private lateinit var firebaseListAdapter: FirebaseRecyclerAdapter<Message, MessageViewHolder>
    lateinit var imageUri: Uri
    var downloadUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityMainContainer = binding.activityContainer

        checkUserAutiroseition()

        displayAllMessages()

       onViewClick()

    }

    private fun onViewClick() {
        with(binding) {
            btnSend.setOnClickListener {
                with(binding) {
                    if (editMessageText.text.toString() != "") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            FirebaseDatabase.getInstance().getReference().push().setValue(
                                Message(
                                    FirebaseAuth.getInstance().currentUser?.email ?: "",
                                    editMessageText.text.toString(),
                                    downloadUri ?: ""
                                )
                            )
                        }
//                    firebaseListAdapter.notifyDataSetChanged()
                        editMessageText.setText("")
                        displayAllMessages()
                    }
                }
                it.hideKeyboard()
            }

            chooseFile.setOnClickListener {
                checkReadExternalStoragePermission()
            }
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

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            binding.listItemsLayout.messageImage.setImageURI(imageUri)

            uploadImage(imageUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImage(imageUri: Uri) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading file...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formater = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formater.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        storageReference.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
            binding.listItemsLayout.messageImage.setImageURI(null)
            Toast.makeText(this, "Upload successful", Toast.LENGTH_LONG).show()

            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // add the message to Firebase Realtime Database with the download URL of the image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    FirebaseDatabase.getInstance().getReference().push().setValue(
                        Message(
                            FirebaseAuth.getInstance().currentUser?.email ?: "",
                            binding.editMessageText.text.toString(),
                            downloadUrl
                        )
                    )
                }
            }
            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnFailureListener{
            progressDialog.dismiss()
            Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show()
        }
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

    private fun checkUserAutiroseition() {
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
    }

    private fun checkReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_MEDIA_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    ACCESS_MEDIA_LOCATION
                )
            ) {
                // Show an explanation to the user why the permission is needed
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        ACCESS_MEDIA_LOCATION,
                        READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE,
                        CAMERA,
                        READ_MEDIA_IMAGES
                    ),
                    READ_EXTERNAL_STORAGE_PERMISSION_CODE
                )
            }
        } else {
            // Permission has already been granted
            openGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted
                openGallery()
            } else {
                // Permission has been denied
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }
}