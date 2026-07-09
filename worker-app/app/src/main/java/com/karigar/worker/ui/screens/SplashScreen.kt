package com.karigar.worker.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.karigar.worker.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.TokenStore
import com.karigar.worker.data.remote.ApiClient
import com.karigar.worker.navigation.Routes
import com.karigar.worker.ui.theme.brandHeaderBrush
import kotlinx.coroutines.delay
import retrofit2.HttpException

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500))
        scale.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(500)

        val store = TokenStore(context)
        val token = store.getToken()
        val dest = if (!token.isNullOrBlank()) {
            try {
                if (ApiClient.api.workerMe("Bearer $token").success) Routes.HOME else Routes.LOGIN
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403 || e.code() == 404) {
                    store.clear(); Routes.LOGIN
                } else Routes.HOME
            } catch (e: Exception) {
                Routes.HOME
            }
        } else {
            Routes.LOGIN
        }
        onNavigate(dest)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(brandHeaderBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.karigar_logo),
                contentDescription = "Karigar Partner",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clip(RoundedCornerShape(32.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Karigar Partner", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.alpha(alpha.value))
            Spacer(modifier = Modifier.height(6.dp))
            Text("Earn with your skills", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.alpha(alpha.value))
        }
    }
}
