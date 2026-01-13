package com.example.sproutly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.UserRemote

@Composable
fun ProfileScreen(
    currentUser: UserRemote?,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Text("Nome: ${currentUser?.name ?: "—"}")
        Text("Email: ${currentUser?.email ?: "—"}")
        Text("Role: ${currentUser?.role ?: "—"}")

        Spacer(Modifier.height(20.dp))

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}
