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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.Categories
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.WorkerOrderDto
import com.karigar.worker.ui.categoryIcon
import com.karigar.worker.ui.components.money
import com.karigar.worker.ui.theme.brandHeaderBrush

private val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 10) return ""
    return try {
        val d = iso.substring(0, 10).split("-")
        val month = MONTHS.getOrElse(d[1].toInt() - 1) { d[1] }
        "${d[2]} $month ${d[0]}"
    } catch (_: Exception) {
        iso.substring(0, 10)
    }
}

@Composable
fun WorkerHistoryScreen(bearer: String) {
    var loading by remember { mutableStateOf(true) }
    var history by remember { mutableStateOf<List<WorkerOrderDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            history = ApiClient.api.getWorkerOrders(bearer).orders
                .filter { it.status == "completed" || it.status == "cancelled" }
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(brandHeaderBrush())
                .padding(start = 20.dp, end = 20.dp, top = 34.dp, bottom = 22.dp)
        ) {
            Text("Job History", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Your completed and past jobs", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No completed jobs yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                    Text("Your finished work will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(history) { order -> HistoryCard(order) }
            }
        }
    }
}

@Composable
private fun HistoryCard(order: WorkerOrderDto) {
    val cancelled = order.status == "cancelled"
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(order.category ?: ""), null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(Categories.label(order.category), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(formatDate(order.completedAt ?: order.createdAt), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (cancelled) {
                    Text("Cancelled", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                } else {
                    Text("₹${money(order.earning)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    Text("Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
