package com.example.sproutly.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGardenScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var motivation by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Horta") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Voltar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Localização") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = motivation,
                onValueChange = { motivation = it },
                label = { Text("Motivação") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    enabled = !loading
                ) {
                    Text(if (selectedImageUri == null) "Escolher foto" else "Foto selecionada")
                }

                if (selectedImageUri != null) {
                    OutlinedButton(
                        onClick = { selectedImageUri = null },
                        enabled = !loading
                    ) {
                        Text("Remover")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    if (name.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Nome e localização são obrigatórios.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val sp = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
                    val ownerEmailStr = sp.getString("USER_EMAIL", "") ?: ""

                    if (ownerEmailStr.isBlank()) {
                        Toast.makeText(context, "Sessão inválida (sem email).", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true

                    scope.launch {
                        try {
                            // RequestBody (text/plain)
                            val rbName = name.toRequestBody("text/plain".toMediaTypeOrNull())
                            val rbLocation = location.toRequestBody("text/plain".toMediaTypeOrNull())
                            val rbDescription = description.toRequestBody("text/plain".toMediaTypeOrNull())
                            val rbMotivation = motivation.toRequestBody("text/plain".toMediaTypeOrNull())
                            val rbOwnerEmail = ownerEmailStr.toRequestBody("text/plain".toMediaTypeOrNull())

                            // Foto opcional
                            val photoPart: MultipartBody.Part? =
                                if (selectedImageUri != null) {
                                    val bytes = context.contentResolver.openInputStream(selectedImageUri!!)?.use { it.readBytes() }
                                        ?: byteArrayOf()

                                    val reqFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                    MultipartBody.Part.createFormData(
                                        name = "photo",
                                        filename = "garden.jpg",
                                        body = reqFile
                                    )
                                } else null

                            val response = RetrofitClient.api.createGarden(
                                name = rbName,
                                location = rbLocation,
                                description = rbDescription,
                                motivation = rbMotivation,
                                ownerEmail = rbOwnerEmail,
                                photo = photoPart
                            )

                            val body = response.body()

                            if (response.isSuccessful && body != null && body.status == "success") {
                                Toast.makeText(context, "Horta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                onCreated()
                            } else {
                                val msg = body?.message ?: "Erro ao criar horta (HTTP ${response.code()})"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text(if (loading) "A criar..." else "Criar")
            }
        }
    }
}
