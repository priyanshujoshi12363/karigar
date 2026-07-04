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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.WorkerStatsDto
import com.karigar.worker.ui.components.money
import com.karigar.worker.ui.theme.brandHeaderBrush

@Composable
fun WorkerStatsScreen(bearer: String) {
    var loading by remember { mutableStateOf(true) }
    var stats by remember { mutableStateOf<WorkerStatsDto?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            stats = ApiClient.api.getStats(bearer).stats
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
            Text("Earnings & Stats", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Your performance so far", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val s = stats
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text("Total Earnings", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("₹${money(s?.totalEarnings)}", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Lifetime payout received", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MiniStat("Today", "₹${money(s?.todayEarnings)}", Modifier.weight(1f))
                    MiniStat("This week", "₹${money(s?.weekEarnings)}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                Text("Jobs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))
                StatRow(Icons.Filled.CheckCircle, Color(0xFF16A34A), "Completed jobs", (s?.completedJobs ?: 0).toString())
                Spacer(Modifier.height(10.dp))
                StatRow(Icons.Filled.Bolt, MaterialTheme.colorScheme.primary, "Active jobs", (s?.activeJobs ?: 0).toString())
                Spacer(Modifier.height(10.dp))
                StatRow(Icons.Filled.Cancel, Color(0xFFDC2626), "Cancelled", (s?.cancelledJobs ?: 0).toString())
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, tint: Color, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(14.dp))
            Text(label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
