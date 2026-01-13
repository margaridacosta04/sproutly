package com.example.sproutly.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sproutly.network.*
import com.example.sproutly.ui.theme.ForestBackground
import com.example.sproutly.ui.theme.ForestGreenDark
import com.example.sproutly.ui.theme.ForestGreenPrimary
import com.example.sproutly.ui.theme.TextDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NotificationsScreen(
    currentUser: UserRemote,
    onOpenGarden: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<NotificationRemote>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    var selected by remember { mutableStateOf<NotificationRemote?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Detalhes carregados conforme o tipo
    var gardenDetail by remember { mutableStateOf<GardenRemote?>(null) }
    var joinDetail by remember { mutableStateOf<JoinRequestRemote?>(null) }

    var isFetchingDetail by remember { mutableStateOf(false) }
    var detailError by remember { mutableStateOf<String?>(null) }

    // Rejeição
    var rejectMode by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun refresh() {
        loading = true
        scope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitClient.api.getNotifications(currentUser.email)
                withContext(Dispatchers.Main) {
                    notifications = res.notifications
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toast("Erro a carregar notificações: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) { loading = false }
            }
        }
    }

    fun openNotification(n: NotificationRemote) {
        selected = n
        showDialog = true

        // reset de estados do diálogo
        rejectMode = false
        rejectReason = ""
        gardenDetail = null
        joinDetail = null
        isFetchingDetail = false
        detailError = null

        // Se for ACTION, tentamos carregar detalhes (para o diálogo não ficar vazio)
        if (n.type == "ACTION") {
            isFetchingDetail = true
            scope.launch(Dispatchers.IO) {
                try {
                    // Fluxo A: join request
                    if (n.related_join_request_id != null) {
                        val joinRes = RetrofitClient.api.getJoinRequestDetail(n.related_join_request_id!!)
                        joinDetail = joinRes.request

                        // tenta também buscar garden detail para saber managerUserId etc
                        val gId = joinRes.request?.garden_id
                        if (gId != null && gId > 0) {
                            val gRes = RetrofitClient.api.getGardenDetail(gId, currentUser.email)
                            gardenDetail = gRes.garden
                        }
                    }
                    // Fluxo B: criação de horta (pendente)
                    else if (n.related_garden_id != null) {
                        val gRes = RetrofitClient.api.getGardenDetail(n.related_garden_id!!, currentUser.email)
                        gardenDetail = gRes.garden
                    }
                } catch (e: Exception) {
                    detailError = e.message ?: "Falha a carregar detalhes"
                } finally {
                    withContext(Dispatchers.Main) { isFetchingDetail = false }
                }
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestBackground)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Notificações",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestGreenDark
            )
            TextButton(onClick = { refresh() }, enabled = !loading) {
                Text(if (loading) "..." else "Atualizar")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (!loading && notifications.isEmpty()) {
            Text("Sem notificações.", color = TextDark)
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notifications) { n ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openNotification(n) },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(n.title, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        Text(n.message, color = TextDark)
                        if (n.type == "ACTION") {
                            Spacer(Modifier.height(6.dp))
                            Text("Toque para decidir", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // ---------------- DIALOG ----------------
    if (showDialog && selected != null) {
        val n = selected!!

        val isAdmin = currentUser.role.trim().equals("ADMIN", ignoreCase = true)
        val isManager = gardenDetail?.managerUserId != null && currentUser.id == gardenDetail?.managerUserId
        val canDecideJoin = isAdmin || isManager

        // para criação de horta: só admin
        val canDecideGarden = isAdmin

        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Notificação") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(n.message)

                    if (isFetchingDetail) {
                        Text("A carregar detalhes...", color = Color.Gray)
                    } else if (detailError != null) {
                        Text("Falha a carregar detalhes: $detailError", color = Color.Red)
                    }

                    // Se temos detalhes de horta, mostramos um resumo
                    if (gardenDetail != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Horta: ${gardenDetail!!.name}", fontWeight = FontWeight.Bold)
                                Text("Local: ${gardenDetail!!.location}")
                            }
                        }
                        TextButton(onClick = { onOpenGarden(gardenDetail!!.id) }) {
                            Text("Abrir horta")
                        }
                    }

                    // Se for pedido de adesão, mostramos dados do pedido
                    if (joinDetail != null) {
                        val r = joinDetail!!
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Pedido de adesão", fontWeight = FontWeight.Bold)
                                Text("Nome: ${r.requester_name}")
                                Text("Idade: ${r.requester_age}")
                                Text("Objetivo: ${r.objective}")
                                Text("Telefone: ${r.phone}")
                                Text("Email: ${r.email}")
                            }
                        }
                    }

                    // Campo de rejeição: aparece SEMPRE quando rejectMode=true
                    if (rejectMode) {
                        OutlinedTextField(
                            value = rejectReason,
                            onValueChange = { rejectReason = it },
                            label = { Text("Motivo da recusa (obrigatório)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },

            // --------- BOTÕES ---------
            confirmButton = {
                // Caso INFO
                if (n.type != "ACTION") {
                    Button(onClick = { showDialog = false }) { Text("OK") }
                    return@AlertDialog
                }

                // ACTION: decide qual fluxo
                val isJoinAction = n.related_join_request_id != null
                val isGardenAction = (n.related_join_request_id == null && n.related_garden_id != null)

                // Se ainda está a carregar detalhe, deixa fechar (evita “preso”)
                if (isFetchingDetail) {
                    Button(onClick = { showDialog = false }) { Text("Fechar") }
                    return@AlertDialog
                }

                // Join Request
                if (isJoinAction) {
                    if (!canDecideJoin) {
                        Button(onClick = { showDialog = false }) { Text("Fechar") }
                    } else {
                        if (!rejectMode) {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            RetrofitClient.api.respondJoinRequest(
                                                RespondJoinRequest(
                                                    requestId = n.related_join_request_id!!,
                                                    action = "ACCEPT",
                                                    reason = "",
                                                    reviewerEmail = currentUser.email
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                toast("Aceite!")
                                                showDialog = false
                                                refresh()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { toast("Erro: ${e.message}") }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) { Text("Aceitar") }
                        } else {
                            Button(
                                onClick = {
                                    if (rejectReason.trim().isEmpty()) {
                                        toast("Motivo obrigatório")
                                        return@Button
                                    }
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            RetrofitClient.api.respondJoinRequest(
                                                RespondJoinRequest(
                                                    requestId = n.related_join_request_id!!,
                                                    action = "REJECT",
                                                    reason = rejectReason.trim(),
                                                    reviewerEmail = currentUser.email
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                toast("Recusado.")
                                                showDialog = false
                                                refresh()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { toast("Erro: ${e.message}") }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Confirmar recusa") }
                        }
                    }
                    return@AlertDialog
                }

                // Garden creation (admin review)
                if (isGardenAction) {
                    if (!canDecideGarden) {
                        Button(onClick = { showDialog = false }) { Text("Fechar") }
                    } else {
                        if (!rejectMode) {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            RetrofitClient.api.adminReviewGarden(
                                                AdminReviewGardenBody(
                                                    gardenId = n.related_garden_id!!,
                                                    action = "APPROVE",
                                                    reason = "",
                                                    reviewerEmail = currentUser.email
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                toast("Horta aprovada!")
                                                showDialog = false
                                                refresh()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { toast("Erro: ${e.message}") }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                            ) { Text("Aprovar") }
                        } else {
                            Button(
                                onClick = {
                                    if (rejectReason.trim().isEmpty()) {
                                        toast("Motivo obrigatório")
                                        return@Button
                                    }
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            RetrofitClient.api.adminReviewGarden(
                                                AdminReviewGardenBody(
                                                    gardenId = n.related_garden_id!!,
                                                    action = "REJECT",
                                                    reason = rejectReason.trim(),
                                                    reviewerEmail = currentUser.email
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                toast("Horta rejeitada.")
                                                showDialog = false
                                                refresh()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { toast("Erro: ${e.message}") }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Confirmar recusa") }
                        }
                    }
                    return@AlertDialog
                }

                // fallback
                Button(onClick = { showDialog = false }) { Text("Fechar") }
            },

            dismissButton = {
                // Botão secundário alterna modo recusar/cancelar recusa
                val isAdmin = currentUser.role.trim().equals("ADMIN", ignoreCase = true)
                val isJoinAction = n.related_join_request_id != null
                val isGardenAction = (n.related_join_request_id == null && n.related_garden_id != null)

                val canToggleReject =
                    (isJoinAction && (isAdmin || (gardenDetail?.managerUserId != null && currentUser.id == gardenDetail?.managerUserId))) ||
                            (isGardenAction && isAdmin)

                if (n.type == "ACTION" && canToggleReject && !isFetchingDetail) {
                    TextButton(onClick = { rejectMode = !rejectMode }) {
                        Text(if (rejectMode) "Cancelar" else "Recusar", color = Color.Red)
                    }
                } else {
                    TextButton(onClick = { showDialog = false }) { Text("Fechar") }
                }
            }
        )
    }
}
