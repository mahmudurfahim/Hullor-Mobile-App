package com.hullor.app

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 🔕 DO NOTHING
        // Firebase Console notification is already shown by Android

        Log.d("FCM", "Message received from console")
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
    }
}
