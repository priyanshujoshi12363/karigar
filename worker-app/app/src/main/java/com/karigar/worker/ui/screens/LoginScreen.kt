package com.karigar.worker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.R
import com.karigar.worker.auth.PhoneAuthenticator
import com.karigar.worker.auth.findActivity
import com.karigar.worker.data.LegalDoc
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.LoginRequest
import com.karigar.worker.ui.components.ConsentRow
import com.karigar.worker.ui.components.PrimaryButton
import com.karigar.worker.ui.theme.brandHeaderBrush
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onRegister: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val store = remember { TokenStore(context) }
    val phoneAuth = remember { activity?.let { PhoneAuthenticator(it) } }

    var otpSent by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var agreed by remember { mutableStateOf(false) }
    var legalDoc by remember { mutableStateOf<LegalDoc?>(null) }

    fun doLogin(idToken: String) {
        scope.launch {
            try {
                val resp = ApiClient.api.workerLogin(LoginRequest(phone = "+91$phone", idToken = idToken))
                val token = resp.token
                if (resp.success && !token.isNullOrBlank()) {
                    store.saveToken(token)
                    onLoggedIn()
                } else {
                    error = resp.message ?: "Login failed"
                }
            } catch (e: HttpException) {
                error = if (e.code() == 404) "No partner account found. Please register." else "Server error (${e.code()})"
            } catch (e: Exception) {
                error = "Cannot reach server. Try again."
            } finally {
                loading = false
            }
        }
    }

    fun sendOtp() {
        error = null
        loading = true
        phoneAuth?.sendCode(
            phoneE164 = "+91$phone",
            onCodeSent = { loading = false; otpSent = true },
            onError = { loading = false; error = it },
            onVerified = { doLogin(it) }
        ) ?: run { loading = false; error = "Could not start verification" }
    }

    fun verifyOtp() {
        error = null
        loading = true
        phoneAuth?.verify(
            code = otp,
            onError = { loading = false; error = it },
            onVerified = { doLogin(it) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(brandHeaderBrush())
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.karigar_logo),
                    contentDescription = "Karigar Partner",
                    modifier = Modifier.size(84.dp).clip(RoundedCornerShape(22.dp))
                )
                Spacer(Modifier.height(16.dp))
                Text("Karigar Partner", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text("Login to manage your jobs", fontSize = 14.sp, color = Color.White, textAlign = TextAlign.Center)
            }
        }

        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp)) {
            AnimatedContent(
                targetState = otpSent,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally { -it } + fadeOut(tween(300)))
                },
                label = "loginStep"
            ) { showOtp ->
                if (!showOtp) {
                    Column {
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
                        Spacer(Modifier.height(18.dp))
                        ConsentRow(agreed = agreed, onCheckedChange = { agreed = it }, onOpenDoc = { legalDoc = it })
                        Spacer(Modifier.height(18.dp))
                        PrimaryButton(text = "Send OTP", enabled = phone.length == 10 && agreed, loading = loading) { sendOtp() }
                        if (error != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(error!!, color = Color(0xFFD32F2F), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Column {
                        Text("Verify your number", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(4.dp))
                        Text("Enter the code sent to +91 $phone", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        PrimaryButton(text = "Verify & Login", enabled = otp.length >= 6, loading = loading) { verifyOtp() }
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
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("New here? ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Register as Partner",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onRegister() }
                    )
                }
            }
        }
    }

    legalDoc?.let { doc ->
        LegalScreen(doc = doc, onBack = { legalDoc = null })
    }
    }
}
