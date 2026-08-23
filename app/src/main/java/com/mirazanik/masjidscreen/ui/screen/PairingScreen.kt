package com.mirazanik.masjidscreen.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.mirazanik.masjidscreen.ui.component.QrCodeImage
import com.mirazanik.masjidscreen.util.PairingQr
import com.mirazanik.masjidscreen.viewmodel.ScreenPairingState

@Composable
fun PairingScreen(
    pairingState: ScreenPairingState,
    deviceId: String,
    onConnect: (String) -> Unit,
    onClearError: () -> Unit,
    onEnterAdmin: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    val isLoading = pairingState is ScreenPairingState.Pairing
    val errorMessage = (pairingState as? ScreenPairingState.PairingError)?.message

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) code = ""
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val landscape = maxWidth >= maxHeight
        val qrSize = if (landscape) {
            min(maxHeight - 48.dp, maxWidth * 0.38f)
        } else {
            min(maxHeight * 0.42f, maxWidth - 48.dp)
        }

        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (landscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    QrPanel(
                        deviceId = deviceId,
                        qrSize = qrSize,
                        modifier = Modifier.weight(1f)
                    )
                    PairingControls(
                        code = code,
                        onCodeChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                code = it
                                if (pairingState is ScreenPairingState.PairingError) onClearError()
                            }
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onConnect = { if (code.length == 6) onConnect(code) },
                        onEnterAdmin = onEnterAdmin,
                        modifier = Modifier.weight(1.15f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    QrPanel(deviceId = deviceId, qrSize = qrSize)
                    PairingControls(
                        code = code,
                        onCodeChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                code = it
                                if (pairingState is ScreenPairingState.PairingError) onClearError()
                            }
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onConnect = { if (code.length == 6) onConnect(code) },
                        onEnterAdmin = onEnterAdmin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun QrPanel(
    deviceId: String,
    qrSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (deviceId.isNotBlank()) {
            QrCodeImage(
                content = PairingQr.payload(deviceId),
                modifier = Modifier.size(qrSize)
            )
        } else {
            Box(
                modifier = Modifier.size(qrSize),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Waiting for admin to scan",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PairingControls(
    code: String,
    onCodeChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onConnect: () -> Unit,
    onEnterAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Connect Screen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Scan the QR from the admin panel, or enter a pairing code.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = "Pairing code",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = {
                Text(
                    "*******",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onEnterAdmin,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Admin")
            }
            Button(
                onClick = onConnect,
                enabled = code.length == 6 && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Connect")
                }
            }
        }
    }
}
