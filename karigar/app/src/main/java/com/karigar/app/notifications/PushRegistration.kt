package com.karigar.app.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.karigar.app.data.TokenStore
import com.karigar.app.data.remote.ApiClient
import com.karigar.app.data.remote.PushTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun registerFcmToken(context: Context) {
    val jwt = TokenStore(context).getToken() ?: return
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) return@addOnCompleteListener
        val fcm = task.result ?: return@addOnCompleteListener
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.api.savePushToken("Bearer $jwt", PushTokenRequest(fcm))
            } catch (_: Exception) {
            }
        }
    }
}
