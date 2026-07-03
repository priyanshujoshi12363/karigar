package com.karigar.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.karigar.app.data.TokenStore
import com.karigar.app.data.remote.ApiClient
import com.karigar.app.data.remote.LoginRequest
import com.karigar.app.data.remote.RegisterRequest
import com.karigar.app.ui.components.PrimaryButton
import com.karigar.app.ui.theme.BluePrimary
import com.karigar.app.ui.theme.brandHeaderBrush
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AuthScreen(onAuthed: () -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var otpSent by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    val store = remember { TokenStore(context) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun callBackend(idToken: String?) {
        scope.launch {
            try {
                val resp = if (isRegister) {
                    ApiClient.api.register(
                        RegisterRequest(
                            phone = "+91$phone",
                            name = name,
                            idToken = idToken,
                            address = address.ifBlank { null }
                        )
                    )
                } else {
                    ApiClient.api.login(LoginRequest(phone = "+91$phone", idToken = idToken))
                }
                val token = resp.token
                if (resp.success && !token.isNullOrBlank()) {
                    store.saveToken(token)
                    onAuthed()
                } else {
                    error = resp.message ?: "Something went wrong"
                }
            } catch (e: HttpException) {
                error = when (e.code()) {
                    404 -> "No account found. Please register."
                    409 -> "Already registered. Please login."
                    401 -> "OTP verification failed. Try again."
                    else -> "Server error (${e.code()})"
                }
            } catch (e: Exception) {
                error = "Cannot reach server. Try again."
            } finally {
                loading = false
            }
        }
    }

    fun signIn(credential: PhoneAuthCredential) {
        loading = true
        error = null
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.user?.getIdToken(false)?.addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        callBackend(t.result?.token)
                    } else {
                        loading = false
                        error = "Verification failed. Try again."
                    }
                } ?: run { loading = false; error = "Verification failed." }
            } else {
                loading = false
                error = "Invalid OTP. Please check and try again."
            }
        }
    }

    fun sendCode() {
        val act = activity
        if (act == null) {
            error = "Could not start verification"
            return
        }
        loading = true
        error = null
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                loading = false
                error = e.localizedMessage ?: "Verification failed"
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                loading = false
                verificationId = id
                otpSent = true
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(act)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode() {
        val id = verificationId
        if (id == null) {
            error = "Please request the OTP again"
            return
        }
        signIn(PhoneAuthProvider.getCredential(id, otp))
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(brandHeaderBrush())
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("K", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (isRegister) "Create your account" else "Welcome back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isRegister) "Sign up to book trusted workers" else "Login to continue to Karigar",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp)) {
            AnimatedContent(
                targetState = otpSent,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally { -it } + fadeOut(tween(300)))
                },
                label = "authStep"
            ) { showOtp ->
                if (!showOtp) {
                    Column {
                        if (isRegister) {
                            KField(name, { name = it }, "Full name", Icons.Filled.Person)
                            Spacer(Modifier.height(14.dp))
                        }
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                            label = { Text("Phone number") },
                            prefix = { Text("+91 ") },
                            leadingIcon = { Icon(Icons.Filled.Phone, null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isRegister) {
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Address") },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                                trailingIcon = {
                                    TextButton(onClick = { address = "Using current location…" }) {
                                        Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(18.dp))
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(28.dp))
                        PrimaryButton(
                            text = "Send OTP",
                            enabled = phone.length == 10 && (!isRegister || name.isNotBlank()),
                            loading = loading
                        ) {
                            error = null
                            sendCode()
                        }
                        if (error != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(error!!, color = Color(0xFFD32F2F), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Column {
                        Text(
                            "Verify your number",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Enter the code sent to +91 $phone",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { if (it.length <= 6) otp = it.filter { c -> c.isDigit() } },
                            label = { Text("OTP") },
                            leadingIcon = { Icon(Icons.Filled.Lock, null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(28.dp))
                        PrimaryButton(
                            text = if (isRegister) "Verify & Create Account" else "Verify & Login",
                            enabled = otp.length >= 6,
                            loading = loading
                        ) {
                            error = null
                            verifyCode()
                        }
                        if (error != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(error!!, color = Color(0xFFD32F2F), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = { otpSent = false; error = null; otp = "" }) { Text("Change number") }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (!otpSent) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isRegister) "Already have an account? " else "Don't have an account? ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (isRegister) "Login" else "Register",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            isRegister = !isRegister
                            otp = ""
                            error = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
