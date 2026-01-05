package com.realityos.realityos.ui.rules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions // THIS IMPORT IS NOW ADDED
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.realityos.realityos.data.local.entity.RuleEntity
import com.realityos.realityos.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    navController: NavController,
    viewModel: RulesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manage Rules") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        if (uiState.rules.isEmpty()) {
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 Text("No rules defined. Tap '+' to create one.")
             }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(8.dp)) {
                items(uiState.rules) { rule ->
                    RuleItem(rule = rule, onDelete = { viewModel.deleteRule(rule) })
                }
            }
        }

        if (showDialog) {
            AddRuleDialog(
                viewModel = viewModel,
                onDismiss = { showDialog = false },
                onConfirm = { rule ->
                    viewModel.addRule(rule)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun RuleItem(rule: RuleEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        ListItem(
            headlineContent = { Text(rule.targetAppPackageName, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Limit: ${rule.timeLimitMinutes} min, Punishment: ${rule.punishmentType}") },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    viewModel: RulesViewModel,
    onDismiss: () -> Unit,
    onConfirm: (RuleEntity) -> Unit
) {
    val context = LocalContext.current
    val installedApps = remember { viewModel.getInstalledApps(context) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var timeLimit by remember { mutableStateOf("30") }
    var punishment by remember { mutableStateOf("GRAYSCALE") }
    var appMenuExpanded by remember { mutableStateOf(false) }
    var punishmentMenuExpanded by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = appMenuExpanded,
                    onExpandedChange = { appMenuExpanded = !appMenuExpanded }
                ) {
                     TextField(
                        value = selectedApp?.appName ?: "Select App",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = appMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = appMenuExpanded,
                        onDismissRequest = { appMenuExpanded = false }
                    ) {
                        installedApps.forEach { app ->
                            DropdownMenuItem(
                                text = { Text(app.appName) },
                                onClick = {
                                    selectedApp = app
                                    appMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = timeLimit,
                    onValueChange = { timeLimit = it.filter { char -> char.isDigit() } },
                    label = { Text("Time Limit (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                ExposedDropdownMenuBox(
                    expanded = punishmentMenuExpanded,
                    onExpandedChange = { punishmentMenuExpanded = !punishmentMenuExpanded }
                ) {
                     TextField(
                        value = punishment,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = punishmentMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = punishmentMenuExpanded,
                        onDismissRequest = { punishmentMenuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("GRAYSCALE") }, onClick = { punishment = "GRAYSCALE"; punishmentMenuExpanded = false })
                        DropdownMenuItem(text = { Text("BLOCK") }, onClick = { punishment = "BLOCK"; punishmentMenuExpanded = false })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val app = selectedApp
                    if (app != null && timeLimit.isNotBlank()) {
                        onConfirm(
                            RuleEntity(
                                targetAppPackageName = app.packageName,
                                timeLimitMinutes = timeLimit.toLong(),
                                punishmentType = punishment
                            )
                        )
                    }
                },
                enabled = selectedApp != null && timeLimit.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
