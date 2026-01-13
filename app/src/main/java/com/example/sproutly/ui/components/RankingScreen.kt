package com.example.sproutly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.UserRemote

@Composable
fun RankingScreen(currentUser: UserRemote?) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ranking", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("User: ${currentUser?.name ?: "—"}")

        Spacer(Modifier.height(16.dp))

        repeat(10) { idx ->
            ListItem(
                headlineContent = { Text("#${idx + 1} Jogador") },
                supportingContent = { Text("Pontuação: ${(10 - idx) * 100}") }
            )
            Divider()
        }
    }
}
