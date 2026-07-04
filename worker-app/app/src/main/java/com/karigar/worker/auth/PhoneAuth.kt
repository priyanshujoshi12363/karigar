package com.karigar.worker.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class PhoneAuthenticator(private val activity: Activity) {
    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null

    fun sendCode(
        phoneE164: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit,
        onVerified: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential, onError, onVerified)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.localizedMessage ?: "Verification failed")
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                onCodeSent()
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verify(code: String, onError: (String) -> Unit, onVerified: (String) -> Unit) {
        val id = verificationId
        if (id == null) {
            onError("Please request the OTP again")
            return
        }
        signIn(PhoneAuthProvider.getCredential(id, code), onError, onVerified)
    }

    private fun signIn(
        credential: PhoneAuthCredential,
        onError: (String) -> Unit,
        onVerified: (String) -> Unit
    ) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.user?.getIdToken(false)?.addOnCompleteListener { t ->
                    val token = t.result?.token
                    if (t.isSuccessful && token != null) onVerified(token)
                    else onError("Verification failed. Try again.")
                } ?: onError("Verification failed.")
            } else {
                onError("Invalid OTP. Please try again.")
            }
        }
    }
}
