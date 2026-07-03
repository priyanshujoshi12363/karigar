package com.karigar.worker.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.PushTokenRequest
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
