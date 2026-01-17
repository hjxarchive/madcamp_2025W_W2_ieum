package com.ieum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ieum.presentation.feature.onboarding.OnboardingScreen
import com.ieum.presentation.navigation.MainScreen
import com.ieum.presentation.theme.IeumColors
import com.ieum.presentation.theme.IeumTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            IeumTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = IeumColors.Background
                ) {
                    IeumApp()
                }
            }
        }
    }
}

@Composable
fun IeumApp() {
    var showOnboarding by remember { mutableStateOf(true) }
    
    if (showOnboarding) {
        OnboardingScreen(
            onStartClick = { showOnboarding = false }
        )
    } else {
        MainScreen()
    }
}
