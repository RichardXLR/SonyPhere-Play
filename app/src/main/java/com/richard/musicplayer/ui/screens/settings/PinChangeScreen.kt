package com.richard.musicplayer.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.richard.musicplayer.ui.utils.backToMain
import com.richard.musicplayer.viewmodels.PrivacySecurityViewModel
import com.richard.musicplayer.ui.component.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinChangeScreen(
    navController: NavController,
    viewModel: PrivacySecurityViewModel = hiltViewModel()
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showCurrentPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alterar PIN") },
                navigationIcon = {
                    com.richard.musicplayer.ui.component.IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PIN Atual
            OutlinedTextField(
                value = currentPin,
                onValueChange = {
                    if (it.length <= 6) currentPin = it
                    errorMessage = null
                },
                label = { Text("PIN Atual") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showCurrentPin) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPin = !showCurrentPin }) {
                        Icon(
                            if (showCurrentPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Novo PIN
            OutlinedTextField(
                value = newPin,
                onValueChange = {
                    if (it.length <= 6) newPin = it
                    errorMessage = null
                },
                label = { Text("Novo PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPin = !showNewPin }) {
                        Icon(
                            if (showNewPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Confirmar PIN
            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    if (it.length <= 6) confirmPin = it
                    errorMessage = null
                },
                label = { Text("Confirmar PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                        Icon(
                            if (showConfirmPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Mensagem de erro
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    when {
                        newPin.length < 4 -> errorMessage = "O PIN deve ter no mínimo 4 dígitos"
                        newPin != confirmPin -> errorMessage = "Os PINs não coincidem"
                        else -> {
                            try {
                                viewModel.updatePin(currentPin, newPin)
                                navController.navigateUp()
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar")
            }

            Text(
                text = "O PIN deve ter entre 4 e 6 dígitos numéricos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 