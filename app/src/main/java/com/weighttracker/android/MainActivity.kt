package com.weighttracker.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.weighttracker.android.core.di.DefaultAppContainer
import com.weighttracker.android.presentation.navigation.AppNavHost
import com.weighttracker.android.presentation.theme.WeightTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            Log.w("MainActivity", "Edge-to-edge not supported or failed on this device", t)
        }

        val container = (application as? WeightTrackerApp)?.container ?: DefaultAppContainer(this)

        setContent {
            WeightTrackerTheme {
                AppNavHost(container = container)
            }
        }
    }
}
