package com.example.sproutly.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.LoginBody
import com.example.sproutly.network.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Preenche os campos.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true
                    scope.launch {
                        try {
                            val res = RetrofitClient.api.login(LoginBody(email, password))
                            val body = res.body()

                            if (res.isSuccessful && body != null && body.status == "success" && body.user != null) {
                                val u = body.user
                                val sp = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
                                sp.edit()
                                    .putInt("USER_ID", u.id)
                                    .putString("USER_NAME", u.name)
                                    .putString("USER_EMAIL", u.email)
                                    .putString("USER_ROLE", u.role)
                                    .putString("USER_PHOTO_URL", u.photoUrl ?: "")
                                    .putInt("USER_SCORE", u.score)
                                    .apply()

                                Toast.makeText(context, "Login OK", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, body?.message ?: "Dados incorretos", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "A entrar..." else "Entrar")
            }
        }
    }
}
