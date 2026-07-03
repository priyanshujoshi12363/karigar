package com.karigar.worker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.Categories
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.JobDto
import com.karigar.worker.data.remote.OfferDto
import com.karigar.worker.data.remote.OtpRequest
import com.karigar.worker.data.remote.RespondRequest
import com.karigar.worker.data.remote.WorkerOrderDto
import com.karigar.worker.ui.categoryIcon
import com.karigar.worker.ui.components.PrimaryButton
import com.karigar.worker.ui.components.money
import com.karigar.worker.ui.components.statusLabel
import com.karigar.worker.ui.theme.brandHeaderBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkerHomeScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bearer = remember { "Bearer " + (TokenStore(context).getToken() ?: "") }

    var loading by remember { mutableStateOf(true) }
    var offers by remember { mutableStateOf<List<OfferDto>>(emptyList()) }
    var jobs by remember { mutableStateOf<List<JobDto>>(emptyList()) }
    var myOrders by remember { mutableStateOf<List<WorkerOrderDto>>(emptyList()) }
    var reload by remember { mutableStateOf(0) }

    var otpForId by remember { mutableStateOf<String?>(null) }
    var otpMode by remember { mutableStateOf("start") }
    var otpValue by remember { mutableStateOf("") }
    var payFor by remember { mutableStateOf<WorkerOrderDto?>(null) }

    LaunchedEffect(reload) {
        loading = true
        try {
            offers = ApiClient.api.getOffers(bearer).offers
            jobs = ApiClient.api.getJobs(bearer).jobs
            myOrders = ApiClient.api.getWorkerOrders(bearer).orders
                .filter { it.status in setOf("assigned", "in_progress", "awaiting_payment") }
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    fun run(block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
            } catch (_: Exception) {
            }
            reload++
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(brandHeaderBrush()).padding(start = 20.dp, end = 12.dp, top = 30.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Work", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White) }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (offers.isNotEmpty()) {
                    item { SectionTitle("New Requests") }
                    items(offers) { offer ->
                        OfferCard(
                            offer = offer,
                            onAccept = { run { ApiClient.api.respondOffer(bearer, offer.id ?: "", RespondRequest("accept")) } },
                            onReject = { run { ApiClient.api.respondOffer(bearer, offer.id ?: "", RespondRequest("reject")) } }
                        )
                    }
                }
                if (jobs.isNotEmpty()) {
                    item { SectionTitle("Open Jobs") }
                    items(jobs) { job ->
                        JobCard(job = job, onPick = { run { ApiClient.api.pickJob(bearer, job.id ?: "") } })
                    }
                }
                item { SectionTitle("My Jobs") }
                if (myOrders.isEmpty()) {
                    item { Text("No active jobs right now", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) }
                } else {
                    items(myOrders) { order ->
                        MyJobCard(
                            order = order,
                            onStart = { otpForId = order.id; otpMode = "start"; otpValue = "" },
                            onFinish = { otpForId = order.id; otpMode = "finish"; otpValue = "" },
                            onCollect = { payFor = order }
                        )
                    }
                }
            }
        }
    }

    if (otpForId != null) {
        AlertDialog(
            onDismissRequest = { otpForId = null },
            title = { Text(if (otpMode == "start") "Enter Start OTP" else "Enter End OTP") },
            text = {
                Column {
                    Text(
                        if (otpMode == "start") "Ask the customer for their Start OTP to begin." else "Ask the customer for their End OTP to finish.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otpValue,
                        onValueChange = { if (it.length <= 4) otpValue = it.filter { c -> c.isDigit() } },
                        label = { Text("4-digit OTP") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = otpValue.length == 4,
                    onClick = {
                        val id = otpForId ?: return@TextButton
                        val code = otpValue
                        val mode = otpMode
                        otpForId = null
                        run {
                            if (mode == "start") ApiClient.api.startWork(bearer, id, OtpRequest(code))
                            else ApiClient.api.finishWork(bearer, id, OtpRequest(code))
                        }
                    }
                ) { Text("Verify") }
            },
            dismissButton = { TextButton(onClick = { otpForId = null }) { Text("Cancel") } }
        )
    }

    payFor?.let { order ->
        PaymentOverlay(
            order = order,
            bearer = bearer,
            onDone = { payFor = null; reload++ }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun IconChip(category: String?) {
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(categoryIcon(category ?: ""), null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun OfferCard(offer: OfferDto, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(offer.category)
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(Categories.label(offer.category), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("${offer.durationMinutes ?: 0} min · ${offer.distanceKm ?: 0.0} km away", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹${money(offer.earning)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) { Text("Accept") }
            }
        }
    }
}

@Composable
private fun JobCard(job: JobDto, onPick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconChip(job.category)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(Categories.label(job.category), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${job.durationMinutes ?: 0} min · ₹${money(job.earning)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onPick) { Text("Pick") }
        }
    }
}

@Composable
private fun MyJobCard(order: WorkerOrderDto, onStart: () -> Unit, onFinish: () -> Unit, onCollect: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(order.category)
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(Categories.label(order.category), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text(order.customer?.phone ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(statusLabel(order.status), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            order.customer?.address?.let { addr ->
                if (addr.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(addr, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            when (order.status) {
                "assigned" -> PrimaryButton(text = "Enter Start OTP") { onStart() }
                "in_progress" -> PrimaryButton(text = "Done Service") { onFinish() }
                "awaiting_payment" -> PrimaryButton(text = "Collect Payment · ₹${money(order.payment?.amount)}") { onCollect() }
            }
        }
    }
}

@Composable
private fun PaymentOverlay(order: WorkerOrderDto, bearer: String, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    val orderId = order.id ?: ""
    var paid by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(paid) {
        if (paid) {
            delay(1100)
            onDone()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xCC000000)), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (paid) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(14.dp))
                    Text("Payment received", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("Service completed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Collect Payment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text("₹${money(order.payment?.amount)}", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Collect cash or UPI from the customer, then tap the button below to complete the order.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton(text = "Amount Paid", loading = loading) {
                        scope.launch {
                            loading = true
                            try {
                                ApiClient.api.confirmPayment(bearer, orderId)
                            } catch (_: Exception) {
                            }
                            loading = false
                            paid = true
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = onDone) { Text("Cancel") }
                }
            }
        }
    }
}
