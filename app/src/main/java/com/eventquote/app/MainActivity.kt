package com.eventquote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.eventquote.app.ui.navigation.NavGraph
import com.eventquote.app.ui.screens.license.LicenseScreen
import com.eventquote.app.ui.theme.EventQuoteTheme
import com.eventquote.app.utils.LicenseManager

/**
 * Single Activity entry point for the EventQuote app.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EventQuoteApplication

        setContent {
            EventQuoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventQuoteApp(factory = app.viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun EventQuoteApp(factory: com.eventquote.app.viewmodel.ViewModelFactory) {
    val context = LocalContext.current
    var isActivated by remember { mutableStateOf(LicenseManager.isActivated(context)) }

    if (!isActivated) {
        LicenseScreen(onActivated = { isActivated = true })
    } else {
        val navController = rememberNavController()
        NavGraph(
            navController = navController,
            factory = factory
        )
    }
}

