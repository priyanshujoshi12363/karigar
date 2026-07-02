package com.karigar.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.karigar.app.data.TokenStore
import com.karigar.app.data.remote.ApiClient
import com.karigar.app.navigation.Routes
import com.karigar.app.ui.theme.BluePrimary
import com.karigar.app.ui.theme.brandHeaderBrush
import kotlinx.coroutines.delay
import retrofit2.HttpException

@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500))
        scale.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, tween(500))
        delay(500)

        val store = TokenStore(context)
        val token = store.getToken()

        val dest = if (!token.isNullOrBlank()) {
            try {
                val resp = ApiClient.api.verify("Bearer $token")
                if (resp.success) Routes.MAIN else Routes.AUTH
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403 || e.code() == 404) {
                    store.clear()
                    if (store.onboardingSeen) Routes.AUTH else Routes.ONBOARDING
                } else {
                    Routes.MAIN
                }
            } catch (e: Exception) {
                Routes.MAIN
            }
        } else {
            if (store.onboardingSeen) Routes.AUTH else Routes.ONBOARDING
        }

        onNavigate(dest)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandHeaderBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "K", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Karigar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Skilled help, on demand",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}
