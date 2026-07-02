package com.karigar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.app.data.Categories
import com.karigar.app.ui.categoryIcon

private data class PastOrder(
    val category: String,
    val date: String,
    val amount: Int,
    val status: String,
    val rating: Int
)

private val pastOrders = listOf(
    PastOrder("ac_technician", "28 Jun 2026", 152, "Completed", 5),
    PastOrder("maid", "22 Jun 2026", 77, "Completed", 4),
    PastOrder("carpenter", "15 Jun 2026", 227, "Completed", 5),
    PastOrder("cook", "09 Jun 2026", 152, "Cancelled", 0),
    PastOrder("plumber", "01 Jun 2026", 77, "Completed", 4)
)

@Composable
fun HistoryScreen() {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabHeader("Booking History")
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pastOrders) { order -> PastOrderCard(order) }
        }
    }
}

@Composable
private fun PastOrderCard(order: PastOrder) {
    val category = Categories.byValue(order.category)
    val cancelled = order.status == "Cancelled"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(order.category), null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category?.label ?: order.category, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(order.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(4.dp))
                if (cancelled) {
                    Text("Cancelled", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F))
                } else {
                    Row {
                        repeat(5) { i ->
                            Icon(
                                Icons.Filled.Star,
                                null,
                                tint = if (i < order.rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Text("₹${order.amount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
