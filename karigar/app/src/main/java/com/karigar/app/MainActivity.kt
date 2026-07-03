package com.karigar.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.karigar.app.notifications.registerFcmToken
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.karigar.app.data.TokenStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karigar.app.data.OrderDraft
import com.karigar.app.navigation.Routes
import com.karigar.app.ui.screens.AuthScreen
import com.karigar.app.ui.screens.LocationConfirmScreen
import com.karigar.app.ui.screens.MainScreen
import com.karigar.app.ui.screens.OnboardingScreen
import com.karigar.app.ui.screens.OrderScreen
import com.karigar.app.ui.screens.SplashScreen
import com.karigar.app.ui.theme.KarigarTheme

class MainActivity : ComponentActivity() {
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        registerFcmToken(this)

        setContent {
            KarigarTheme {
                KarigarApp()
            }
        }
    }
}

@Composable
fun KarigarApp() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val store = remember { TokenStore(context) }

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onNavigate = { dest ->
                nav.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = {
                store.onboardingSeen = true
                nav.navigate(Routes.AUTH) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.AUTH) {
            AuthScreen(onAuthed = {
                registerFcmToken(context)
                nav.navigate(Routes.MAIN) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            })
        }
        composable(Routes.MAIN) {
            MainScreen(
                onOpenCategory = { value ->
                    OrderDraft.categoryValue = value
                    nav.navigate(Routes.LOCATION)
                },
                onLogout = {
                    store.clear()
                    nav.navigate(Routes.AUTH) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOCATION) {
            LocationConfirmScreen(
                onConfirm = { nav.navigate(Routes.ORDER) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.ORDER) {
            OrderScreen(
                onDone = {
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
}
