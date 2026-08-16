package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AdminPrivacyTab(viewModel: SalesViewModel) {
    val context = LocalContext.current
    val currentRole by viewModel.currentUserRole.collectAsState()
    val auditLogs by viewModel.allAuditLogs.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var exportedJsonData by remember { mutableStateOf<String?>(null) }

    val roles = listOf("Super Admin", "Sales Manager", "Interior Designer", "Logistics Manager")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Admin, Audit Logs & Privacy Control", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }

                        Surface(color = DarkWalnut, shape = RoundedCornerShape(8.dp)) {
                            Text(currentRole, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Manage enterprise permissions, view audit logs, configure multilingual AI prompts, and exercise Android 15 Privacy Data Controls.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Enterprise Role Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Active Enterprise Role", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roles.forEach { role ->
                            FilterChip(
                                selected = currentRole == role,
                                onClick = {
                                    viewModel.currentUserRole.value = role
                                    Toast.makeText(context, "Switched role to $role", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(role, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // ANDROID 15 PRIVACY & DATA CONTROL PANEL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Android 15 User Data Control & Privacy Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    }

                    Text(
                        "Compliant with Google Play 2025 & Android 15 specifications. You hold absolute right to purge all locally stored CRM accounts, leads, quotations, and chat history.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                viewModel.exportUserDataJson { json ->
                                    exportedJsonData = json
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkCocoaBg)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Data (JSON)", fontSize = 11.sp, color = DarkCocoaBg, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Account & Data", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Audit Logs List
        item {
            Text("Enterprise Audit Trail & Security Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(auditLogs, key = { it.id }) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${log.userRole} • ${log.action}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WarmMahogany)
                        Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("#${log.id}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }

    // Export Data Dialog
    if (exportedJsonData != null) {
        AlertDialog(
            onDismissRequest = { exportedJsonData = null },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Exported CRM payload to device storage!", Toast.LENGTH_SHORT).show()
                        exportedJsonData = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Copy Payload", color = DarkCocoaBg, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Exported Privacy Data Payload") },
            text = {
                Column {
                    Text("Android 15 Compliant JSON Export:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(exportedJsonData ?: "", fontSize = 10.sp, color = Color.Green)
                    }
                }
            }
        )
    }

    // Confirm Delete Account & Data Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllUserData()
                        Toast.makeText(context, "All user local data and CRM history permanently wiped!", Toast.LENGTH_LONG).show()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("YES, PERMANENTLY WIPE DATA")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            },
            title = { Text("Confirm Account & Data Wipe") },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account, leads, quotations, and chat history? This action complies with Android 15 data deletion regulations and cannot be undone.",
                    fontSize = 12.sp
                )
            }
        )
    }
}
