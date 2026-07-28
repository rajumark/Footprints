package com.example.footprints

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.footprints.llm.LlmEngine
import com.example.footprints.theme.FootprintsTheme
import com.example.footprints.ui.chat.ChatScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContent {
            FootprintsTheme {
                ChatScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LlmEngine.close()
    }
}
