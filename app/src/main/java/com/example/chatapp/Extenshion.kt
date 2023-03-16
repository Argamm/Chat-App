package com.example.chatapp

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.text.SimpleDateFormat
import java.util.*

fun View.hideKeyboardOnClick(activity: Activity) {
    setOnClickListener {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }
}

fun View.hideKeyboard() {
    val inputManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Date.toFormattedString(format: String): String {
    val formatter = SimpleDateFormat(format)
    return formatter.format(this)
}

fun Long.toFormattedDate(): String {
    return Date(this).toFormattedString("MMM dd")
}

fun Long.toFormattedTime(): String {
    return Date(this).toFormattedString("hh:mm:ss a")
}

