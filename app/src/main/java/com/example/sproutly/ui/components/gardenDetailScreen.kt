package com.example.sproutly.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.RetrofitClient
import com.example.sproutly.network.UserRemote
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenDetailScreen(
    gardenId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var gardenName by remember { mutableStateOf("") }
    var members by remember { mutableStateOf<List<UserRemote>>(emptyList()) }

    fun loadGarden() {
        val sp = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
        val myEmail = sp.getString("USER_EMAIL", "") ?: ""

        loading = true
        error = null

        scope.launch {
            try {
                val response = RetrofitClient.api.getGardenDetail(gardenId, myEmail)
                val body = response.body()

                if (response.isSuccessful && body != null && body.status == "success") {
                    gardenName = body.garden.name
                    members = body.members ?: emptyList()
                } else {
                    error = body?.message ?: "Erro HTTP ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Erro de ligação: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(gardenId) { loadGarden() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (gardenName.isNotBlank()) gardenName else "Detalhes da Horta") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> {
                    Text("Erro: $error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { loadGarden() }) { Text("Tentar novamente") }
                }
                else -> {
                    Text("Membros (${members.size})", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    if (members.isEmpty()) Text("Ainda não há membros nesta horta.")
                    else MembersRow(members)
                }
            }
        }
    }
}

@Composable
private fun MembersRow(members: List<UserRemote>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(members) { m ->
            AssistChip(onClick = {}, label = { Text(m.name) })
        }
    }
}
