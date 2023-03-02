package com.example.chatapp

import android.Manifest.permission.*
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.notification.NotificationData
import com.example.chatapp.notification.PushNotification
import com.example.chatapp.notification.RetrofitInstance
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


const val TOPIC = "/topics/my_topic"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val signInCode: Int = 1
    private lateinit var activityMainContainer: ConstraintLayout
    private lateinit var firebaseListAdapter: FirebaseRecyclerAdapter<MyMessage, MessageViewHolder>
    lateinit var imageUri: Uri
    var downloadUri: String? = null
    var id = Random().nextInt()
//    var myToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//for knowing device token
        /* FirebaseMessaging.getInstance().token.addOnCompleteListener {
             if (!it.isSuccessful) {
                 Log.e("TokenDetails", "token failed to receive${it.result}")
             }
             myToken = it.result
             Log.e("TOKEN", it.result)
         }
         */

        activityMainContainer = binding.activityContainer

        checkUserAutiroseition()

        displayAllMessages()

        onViewClick()
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
                val responce = RetrofitInstance.api.postNotification(notification)

                if (responce.isSuccessful) {
                    Log.d("MainActivity", "Response: $responce}")
                } else {
                    Log.d("MainActivity", responce.errorBody()!!.string())
                }
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
            } catch (e: Exception) {
                Log.e("MainActivity", e.toString())
            }
        }

    private fun onViewClick() {
        with(binding) {
            btnSend.setOnClickListener {
                if (editMessageText.text.toString() != "") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // Add the device tokens of the intended users in the 'to' field of the payload
                        var payload = PushNotification(
                            TOPIC,
                            NotificationData("Chat App", "You have new massage", "message??")
                        )
                        sendNotification(payload)

                        FirebaseDatabase.getInstance().getReference("messages").push().setValue(
                            MyMessage(
                                id++,
                                FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                                editMessageText.text.toString(),
                                downloadUri ?: ""
                            )
                        )
                    }
//                  firebaseListAdapter.notifyDataSetChanged()
                    editMessageText.setText("")
                    displayAllMessages()
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

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {
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
                    sendNotification(
                        PushNotification(
                            TOPIC,
                            NotificationData("Chat App", "You have new massage", "message??")
                        )
                    )

                    FirebaseDatabase.getInstance().getReference("messages").push().setValue(
                        MyMessage(
                            id++,
                            FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                            binding.editMessageText.text.toString(),
                            downloadUrl
                        )
                    )
                }
            }
            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Upload failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayAllMessages() {
        val query = FirebaseDatabase.getInstance().getReference("messages")
        val options = FirebaseRecyclerOptions.Builder<MyMessage>()
            .setQuery(query, MyMessage::class.java).setLifecycleOwner(this)
            .build()

        val messageList = binding.messageRecyclerView

        firebaseListAdapter = MessageAdapter(messageList, options)

        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = firebaseListAdapter

       getSwipeToDelete()
    }

    private fun getSwipeToDelete() {
        val swipeHandler = object : SwipeToDeleteCallback(firebaseListAdapter as MessageAdapter, binding.messageRecyclerView) {
            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                val item = adapter.getItem(position)
                if (item != null) {
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
                return 0
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.messageRecyclerView)
    }

    override fun onStart() {
        super.onStart()
        firebaseListAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
//        firebaseListAdapter.stopListening()
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_MEDIA_LOCATION)) {
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