package com.karigar.worker.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.karigar.worker.data.Categories
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.data.remote.WorkerProfileDto
import com.karigar.worker.ui.theme.brandHeaderBrush

private val PMONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun memberSince(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 10) return "—"
    return try {
        val d = iso.substring(0, 10).split("-")
        "${PMONTHS.getOrElse(d[1].toInt() - 1) { d[1] }} ${d[0]}"
    } catch (_: Exception) {
        iso.substring(0, 10)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerProfileScreen(bearer: String, onLogout: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<WorkerProfileDto?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            profile = ApiClient.api.workerMe(bearer).worker
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(brandHeaderBrush())
                .padding(top = 40.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(46.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(profile?.name?.ifBlank { "Partner" } ?: "Partner", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Phone, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.size(6.dp))
                Text(profile?.phone ?: "", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
            if (profile?.isVerified == true) {
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Verified, null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Verified partner", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("My skills", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(10.dp))
                        val cats = profile?.categories ?: emptyList()
                        if (cats.isEmpty()) {
                            Text("No skills added", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                cats.forEach { c ->
                                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(
                                            Categories.label(c),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                InfoRow(Icons.Filled.WorkHistory, "Experience", "${profile?.experienceYears ?: 0} years")
                if (profile?.workedWithCompany == true && !profile?.companyName.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    InfoRow(Icons.Filled.Business, "Company", profile?.companyName ?: "")
                }
                Spacer(Modifier.height(10.dp))
                InfoRow(Icons.Filled.CalendarMonth, "Partner since", memberSince(profile?.createdAt))

                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFDC2626))
                    Spacer(Modifier.size(8.dp))
                    Text("Log out", color = Color(0xFFDC2626), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(14.dp))
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
