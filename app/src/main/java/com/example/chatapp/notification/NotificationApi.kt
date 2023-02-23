package com.example.chatapp.notification

import com.example.chatapp.Constants.Companion.CONTENT_TYPE
import com.example.chatapp.Constants.Companion.SERVER_KEY
import com.example.chatapp.Constants.Companion.TOKEN
import retrofit2.Response
import retrofit2.http.Body
import okhttp3.ResponseBody
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization:$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}