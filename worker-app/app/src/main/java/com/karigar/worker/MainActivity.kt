package com.karigar.worker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.karigar.worker.notifications.NotificationChannels
import com.karigar.worker.notifications.registerFcmToken
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karigar.worker.data.TokenStore
import com.karigar.worker.navigation.Routes
import com.karigar.worker.ui.screens.LoginScreen
import com.karigar.worker.ui.screens.RegisterScreen
import com.karigar.worker.ui.screens.SplashScreen
import com.karigar.worker.ui.screens.WorkerMainScreen
import com.karigar.worker.ui.theme.KarigarTheme

class MainActivity : ComponentActivity() {
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationChannels.ensure(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        registerFcmToken(this)

        setContent {
            KarigarTheme {
                WorkerApp()
            }
        }
    }
}

@Composable
fun WorkerApp() {
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
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    registerFcmToken(context)
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = {
                    registerFcmToken(context)
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            WorkerMainScreen(onLogout = {
                store.clear()
                nav.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            })
        }
    }
}
