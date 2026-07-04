package com.karigar.worker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.ui.components.OsmMap
import com.karigar.worker.ui.components.PrimaryButton
import kotlinx.coroutines.delay

@Composable
fun LocationPickerScreen(
    initialLat: Double,
    initialLng: Double,
    onConfirm: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var center by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    fun useDefault() {
        if (center == null) center = initialLat to initialLng
    }

    fun fetchLocation() {
        try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    center = if (loc != null) loc.latitude to loc.longitude else (initialLat to initialLng)
                }
                .addOnFailureListener { useDefault() }
        } catch (e: SecurityException) {
            useDefault()
        } catch (e: Exception) {
            useDefault()
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) fetchLocation() else useDefault()
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) fetchLocation() else permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        delay(6000)
        useDefault()
    }

    val resolved = center
    if (resolved == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Getting your location…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        PickerContent(resolved.first, resolved.second, onConfirm, onDismiss)
    }
}

@Composable
private fun PickerContent(
    startLat: Double,
    startLng: Double,
    onConfirm: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var lat by remember { mutableStateOf(startLat) }
    var lng by remember { mutableStateOf(startLng) }
    var address by remember { mutableStateOf("") }
    var loadingAddress by remember { mutableStateOf(true) }

    LaunchedEffect(lat, lng) {
        loadingAddress = true
        delay(800)
        try {
            val resp = ApiClient.api.reverseGeocode(lat, lng)
            address = resp.address?.formatted ?: ""
        } catch (_: Exception) {
            address = ""
        } finally {
            loadingAddress = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        OsmMap(
            latitude = startLat,
            longitude = startLng,
            modifier = Modifier.fillMaxSize()
        ) { la, ln ->
            lat = la
            lng = ln
        }

        Box(
            modifier = Modifier.align(Alignment.Center).offset(y = (-18).dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = "Pin",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(16.dp).size(44.dp).align(Alignment.TopStart)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Set your work location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Move the map so the pin is on your area",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        when {
                            loadingAddress -> Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.size(8.dp))
                                Text("Fetching address…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            address.isNotBlank() -> Text(address, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            else -> Text(
                                "Couldn't fetch address — your pinned location will be used",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "%.5f, %.5f".format(lat, lng),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Confirm Location") {
                    val finalAddress = address.ifBlank { "Pinned location (%.5f, %.5f)".format(lat, lng) }
                    onConfirm(lat, lng, finalAddress)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
