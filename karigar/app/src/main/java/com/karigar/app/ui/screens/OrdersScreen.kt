package com.karigar.app.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.app.data.Categories
import com.karigar.app.data.TokenStore
import com.karigar.app.data.remote.ApiClient
import com.karigar.app.data.remote.BoostRequest
import com.karigar.app.data.remote.OrderDto
import com.karigar.app.ui.categoryIcon
import com.karigar.app.ui.theme.brandHeaderBrush

private val activeStatuses = setOf("searching", "assigned", "in_progress", "awaiting_payment", "open", "expired")

fun statusLabel(status: String?): String = when (status) {
    "searching" -> "Finding worker"
    "assigned" -> "Assigned"
    "in_progress" -> "In progress"
    "awaiting_payment" -> "Awaiting payment"
    "open" -> "In job pool"
    "expired" -> "No worker found"
    "completed" -> "Completed"
    "cancelled" -> "Cancelled"
    else -> status ?: ""
}

fun money(v: Double?): String {
    if (v == null) return "-"
    return if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}

@Composable
fun OrdersScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bearer = remember { "Bearer " + (TokenStore(context).getToken() ?: "") }
    var loading by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    var prevActive by remember { mutableStateOf<Set<String>>(emptySet()) }
    var firstLoad by remember { mutableStateOf(true) }
    var completedNotice by remember { mutableStateOf<String?>(null) }
    var boostingId by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        try {
            orders = ApiClient.api.getOrders(bearer).orders.filter { it.status in activeStatuses }
        } catch (_: Exception) {
        }
    }

    fun boost(orderId: String, amount: Int) {
        scope.launch {
            boostingId = orderId
            try {
                ApiClient.api.boostOrder(bearer, orderId, BoostRequest(amount))
            } catch (_: Exception) {
            }
            boostingId = null
            refresh()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val all = ApiClient.api.getOrders(bearer).orders
                val byId = all.associateBy { it.id }
                if (!firstLoad) {
                    val justDone = prevActive.firstOrNull { id -> byId[id]?.status == "completed" }
                    if (justDone != null) {
                        val cat = byId[justDone]?.category
                        completedNotice = Categories.byValue(cat ?: "")?.label ?: "Your service"
                    }
                }
                orders = all.filter { it.status in activeStatuses }
                prevActive = orders.mapNotNull { it.id }.toSet()
                firstLoad = false
            } catch (_: Exception) {
            } finally {
                loading = false
            }
            delay(5000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabHeader("Active Orders")
        when {
            loading -> LoadingBox()
            orders.isEmpty() -> EmptyState("No active orders", "Book a service and it will show up here")
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders) { order ->
                    ActiveOrderCard(
                        order = order,
                        boosting = boostingId == order.id,
                        onBoost = { amount -> order.id?.let { boost(it, amount) } }
                    )
                }
            }
        }
    }

    if (completedNotice != null) {
        AlertDialog(
            onDismissRequest = { completedNotice = null },
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Payment received") },
            text = { Text("Your ${completedNotice} service is completed and the payment is done. Thank you for using Karigar!") },
            confirmButton = { TextButton(onClick = { completedNotice = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun ActiveOrderCard(order: OrderDto, boosting: Boolean, onBoost: (Int) -> Unit) {
    val category = Categories.byValue(order.category ?: "")
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(categoryIcon(order.category ?: ""), null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category?.label ?: (order.category ?: "Service"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    order.assignedWorker?.name?.let { workerName ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(workerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                StatusPill(statusLabel(order.status))
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val otpToShow = when (order.status) {
                    "assigned" -> order.startOtp
                    "in_progress" -> order.endOtp
                    else -> null
                }
                if (otpToShow != null) {
                    Column {
                        Text(
                            if (order.status == "in_progress") "End OTP" else "Start OTP",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(otpToShow, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    Text("${order.durationMinutes ?: 0} min", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹${money(order.bill?.total)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            if (order.status == "expired") {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(12.dp))
                Text(
                    "No worker accepted your request. Increase the pay to try again — the nearest workers get notified first.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                if (boosting) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(20, 50, 100).forEach { amt ->
                            OutlinedButton(
                                onClick = { onBoost(amt) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                            ) {
                                Text("+₹$amt", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun TabHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brandHeaderBrush())
            .padding(start = 20.dp, end = 20.dp, top = 30.dp, bottom = 18.dp)
    ) {
        Text(title, color = androidx.compose.ui.graphics.Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
