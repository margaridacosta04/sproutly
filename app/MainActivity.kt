package com.example.sproutly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sproutly.ui.SproutlyApp
import com.example.sproutly.ui.theme.SproutlyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SproutlyTheme {
                SproutlyApp()
            }
        }
    }
}
