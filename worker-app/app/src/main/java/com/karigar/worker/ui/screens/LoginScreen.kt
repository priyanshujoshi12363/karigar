package com.karigar.worker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.LoginRequest
import com.karigar.worker.ui.components.PrimaryButton
import com.karigar.worker.ui.theme.BluePrimary
import com.karigar.worker.ui.theme.brandHeaderBrush
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    var otpSent by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { TokenStore(context) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(brandHeaderBrush())
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("K", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
                Spacer(Modifier.height(16.dp))
                Text("Karigar Partner", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Login to manage your jobs", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (!otpSent) {
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
                Spacer(Modifier.height(28.dp))
                PrimaryButton(text = "Send OTP", enabled = phone.length == 10) { otpSent = true }
            } else {
                Text("Verify your number", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Text("Enter the 4-digit code sent to +91 $phone", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 4) otp = it.filter { c -> c.isDigit() } },
                    label = { Text("OTP") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(28.dp))
                PrimaryButton(text = "Verify & Login", enabled = otp.length == 4, loading = loading) {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            val resp = ApiClient.api.workerLogin(LoginRequest(phone = "+91$phone"))
                            val token = resp.token
                            if (resp.success && !token.isNullOrBlank()) {
                                store.saveToken(token)
                                onLoggedIn()
                            } else {
                                error = resp.message ?: "Something went wrong"
                            }
                        } catch (e: HttpException) {
                            error = when (e.code()) {
                                404 -> "Worker not found. Register with the office first."
                                else -> "Server error (${e.code()})"
                            }
                        } catch (e: Exception) {
                            error = "Cannot reach server. Try again."
                        } finally {
                            loading = false
                        }
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(error!!, color = Color(0xFFD32F2F), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { otpSent = false; error = null }) { Text("Change number") }
                }
            }
        }
    }
}
