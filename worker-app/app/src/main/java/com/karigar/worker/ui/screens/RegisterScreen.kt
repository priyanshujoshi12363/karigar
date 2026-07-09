package com.karigar.worker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.auth.PhoneAuthenticator
import com.karigar.worker.auth.findActivity
import com.karigar.worker.data.Categories
import com.karigar.worker.data.LegalDoc
import com.karigar.worker.ui.components.ConsentRow
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.ui.components.PrimaryButton
import com.karigar.worker.ui.theme.brandHeaderBrush
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { TokenStore(context) }
    val activity = remember(context) { context.findActivity() }
    val phoneAuth = remember { activity?.let { PhoneAuthenticator(it) } }
    var showOtpDialog by remember { mutableStateOf(false) }
    var otpValue by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var aadharNumber by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var worked by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }
    val selected: SnapshotStateList<String> = remember { mutableStateListOf() }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var address by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var agreed by remember { mutableStateOf(false) }
    var legalDoc by remember { mutableStateOf<LegalDoc?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    val canSubmit = name.isNotBlank() && phone.length == 10 && aadharNumber.isNotBlank() &&
        selected.isNotEmpty() && photoUri != null && (!worked || companyName.isNotBlank()) && agreed

    fun submit(idToken: String) {
        val uri = photoUri ?: return
        scope.launch {
            loading = true
            error = null
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    error = "Could not read the selected image"
                    loading = false
                    return@launch
                }
                fun textPart(s: String) = s.toRequestBody("text/plain".toMediaTypeOrNull())
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val safeMime = if (mime in listOf("image/jpeg", "image/jpg", "image/png", "image/webp")) mime else "image/jpeg"
                val ext = when {
                    safeMime.contains("png") -> "png"
                    safeMime.contains("webp") -> "webp"
                    else -> "jpg"
                }
                val photoPart = MultipartBody.Part.createFormData(
                    "aadharPhoto", "aadhaar.$ext",
                    bytes.toRequestBody(safeMime.toMediaTypeOrNull())
                )
                val resp = ApiClient.api.registerWorker(
                    phone = textPart("+91$phone"),
                    idToken = textPart(idToken),
                    name = textPart(name),
                    address = if (address.isNotBlank()) textPart(address) else null,
                    coordinates = if (lat != null && lng != null) textPart("[$lng,$lat]") else null,
                    categories = textPart(selected.joinToString(",")),
                    experienceYears = textPart(experience.ifBlank { "0" }),
                    workedWithCompany = textPart(if (worked) "true" else "false"),
                    companyName = if (worked) textPart(companyName) else null,
                    aadharNumber = textPart(aadharNumber),
                    aadharPhoto = photoPart
                )
                val token = resp.token
                if (resp.success && !token.isNullOrBlank()) {
                    store.saveToken(token)
                    onRegistered()
                } else {
                    error = resp.message ?: "Registration failed"
                }
            } catch (e: HttpException) {
                error = when (e.code()) {
                    409 -> "This number is already registered. Please login."
                    400 -> "Please check all details are filled correctly."
                    else -> "Server error (${e.code()})"
                }
            } catch (e: Exception) {
                error = "Cannot reach server. Try again."
            } finally {
                loading = false
            }
        }
    }

    fun startOtp() {
        error = null
        loading = true
        phoneAuth?.sendCode(
            phoneE164 = "+91$phone",
            onCodeSent = { loading = false; otpValue = ""; showOtpDialog = true },
            onError = { loading = false; error = it },
            onVerified = { showOtpDialog = false; submit(it) }
        ) ?: run { loading = false; error = "Could not start verification" }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().background(brandHeaderBrush())
                    .padding(top = 30.dp, bottom = 18.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text("Become a Partner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Field(name, { name = it }, "Full name")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                    label = { Text("Phone number") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                Text("What work do you do?", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Categories.all.forEach { cat ->
                        FilterChip(
                            selected = selected.contains(cat.value),
                            onClick = {
                                if (selected.contains(cat.value)) selected.remove(cat.value)
                                else selected.add(cat.value)
                            },
                            label = { Text(cat.label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Years of experience") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Worked with a company?", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                    Switch(checked = worked, onCheckedChange = { worked = it })
                }
                if (worked) {
                    Spacer(Modifier.height(12.dp))
                    Field(companyName, { companyName = it }, "Company name")
                }

                Spacer(Modifier.height(20.dp))
                Text("Legal (KYC)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = aadharNumber,
                    onValueChange = { if (it.length <= 14) aadharNumber = it },
                    label = { Text("Aadhaar number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { pickImage.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(
                        if (photoUri != null) Icons.Filled.CheckCircle else Icons.Filled.CloudUpload,
                        null
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(if (photoUri != null) "Aadhaar photo selected" else "Upload Aadhaar photo")
                }

                Spacer(Modifier.height(20.dp))
                Text("Work location", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showPicker = true },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle.takeIf { lat != null } ?: Icons.Filled.CloudUpload, null)
                    Spacer(Modifier.size(8.dp))
                    Text(if (address.isNotBlank()) address.take(30) else "Set location on map")
                }

                Spacer(Modifier.height(28.dp))
                ConsentRow(agreed = agreed, onCheckedChange = { agreed = it }, onOpenDoc = { legalDoc = it })
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Create Partner Account", enabled = canSubmit, loading = loading) { startOtp() }
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(error!!, color = Color(0xFFD32F2F), fontSize = 13.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        if (showPicker) {
            LocationPickerScreen(
                initialLat = lat ?: 21.1458,
                initialLng = lng ?: 79.0882,
                onConfirm = { la, ln, addr ->
                    lat = la; lng = ln; address = addr; showPicker = false
                },
                onDismiss = { showPicker = false }
            )
        }

        if (showOtpDialog) {
            AlertDialog(
                onDismissRequest = { showOtpDialog = false; loading = false },
                title = { Text("Verify your number") },
                text = {
                    Column {
                        Text("Enter the code sent to +91 $phone", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = otpValue,
                            onValueChange = { if (it.length <= 6) otpValue = it.filter { c -> c.isDigit() } },
                            label = { Text("OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                    }
                },
                confirmButton = {
                    TextButton(enabled = otpValue.length >= 6, onClick = {
                        showOtpDialog = false
                        loading = true
                        phoneAuth?.verify(
                            code = otpValue,
                            onError = { loading = false; error = it },
                            onVerified = { submit(it) }
                        )
                    }) { Text("Verify") }
                },
                dismissButton = { TextButton(onClick = { showOtpDialog = false; loading = false }) { Text("Cancel") } }
            )
        }

        legalDoc?.let { doc ->
            LegalScreen(doc = doc, onBack = { legalDoc = null })
        }
    }
}

@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
