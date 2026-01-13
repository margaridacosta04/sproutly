package com.example.sproutly

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.sproutly.network.UserRemote
import com.example.sproutly.ui.AppNavHost
import com.example.sproutly.ui.components.LoginScreen
import com.example.sproutly.ui.theme.SproutlyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SproutlyTheme {
                var user by remember { mutableStateOf(loadUserFromPrefs(this)) }

                if (user == null) {
                    LoginScreen(
                        onLoginSuccess = { user = loadUserFromPrefs(this) }
                    )
                } else {
                    AppNavHost(currentUser = user!!)
                }
            }
        }
    }

    private fun loadUserFromPrefs(context: Context): UserRemote? {
        val sp = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)

        val id = sp.getInt("USER_ID", 0)
        val name = sp.getString("USER_NAME", null)
        val email = sp.getString("USER_EMAIL", null)
        val role = sp.getString("USER_ROLE", "USER") ?: "USER"
        val photoUrl = sp.getString("USER_PHOTO_URL", null)
        val score = sp.getInt("USER_SCORE", 0)

        if (name.isNullOrBlank() || email.isNullOrBlank()) return null

        return UserRemote(
            id = id,
            name = name,
            email = email,
            role = role,
            photoUrl = photoUrl,
            score = score
        )
    }
}
