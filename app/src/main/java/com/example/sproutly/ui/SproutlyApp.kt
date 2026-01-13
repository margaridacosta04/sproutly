package com.example.sproutly.ui

import androidx.compose.runtime.Composable
import com.example.sproutly.network.UserRemote
import com.example.sproutly.ui.theme.SproutlyTheme

@Composable
fun SproutlyApp(currentUser: UserRemote) {
    SproutlyTheme {
        AppNavHost(currentUser = currentUser)
    }
}
