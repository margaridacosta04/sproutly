package com.example.sproutly.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sproutly.network.GardenRemote
import com.example.sproutly.network.RetrofitClient
import com.example.sproutly.network.UserRemote
import com.example.sproutly.ui.theme.ForestBackground
import com.example.sproutly.ui.theme.ForestGreenDark
import com.example.sproutly.ui.theme.ForestGreenPrimary
import com.example.sproutly.ui.theme.TextDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    currentUser: UserRemote,
    onAddClick: () -> Unit,
    onGardenClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gardens by remember { mutableStateOf<List<GardenRemote>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun loadGardens() {
        loading = true
        errorMsg = null
        scope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitClient.api.getActiveGardens()
                withContext(Dispatchers.Main) {
                    gardens = res.gardens
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMsg = e.message ?: "Erro ao carregar hortas"
                    Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) { loading = false }
            }
        }
    }

    LaunchedEffect(Unit) { loadGardens() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Text(
                text = "Olá, ${currentUser.name}",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestGreenDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = currentUser.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDark
            )

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hortas ativas",
                    style = MaterialTheme.typography.titleLarge,
                    color = ForestGreenDark
                )
                TextButton(onClick = { loadGardens() }, enabled = !loading) {
                    Text(if (loading) "..." else "Atualizar")
                }
            }

            Spacer(Modifier.height(10.dp))

            when {
                loading && gardens.isEmpty() -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMsg != null && gardens.isEmpty() -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Não foi possível carregar as hortas.", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { loadGardens() }) { Text("Tentar novamente") }
                    }
                }

                gardens.isEmpty() -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ainda não há hortas ativas.", color = TextDark)
                        Spacer(Modifier.height(8.dp))
                        Text("Cria a primeira horta com o botão +.", color = TextDark)
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp) // espaço para o FAB não tapar
                    ) {
                        items(gardens) { g ->
                            GardenCard(garden = g, onClick = { onGardenClick(g.id) })
                        }
                    }
                }
            }
        }

        // FAB simples
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = ForestGreenPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Criar horta")
        }
    }
}

@Composable
private fun GardenCard(
    garden: GardenRemote,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Foto
            if (!garden.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = garden.photoUrl,
                    contentDescription = "Foto da horta",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFEAEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sem foto", color = Color.Gray)
                }
            }

            Column(Modifier.padding(14.dp)) {
                Text(
                    text = garden.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = garden.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
