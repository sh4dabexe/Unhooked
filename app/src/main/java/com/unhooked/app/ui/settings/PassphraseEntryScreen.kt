package com.unhooked.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhooked.app.UnhookedApp
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

/**
 * Manual passphrase entry screen for emergency unlock.
 * Normalizes input (lowercase, collapse whitespace) before verification.
 */
@Composable
fun PassphraseEntryScreen(
    scheduleId: Long,
    onUnlockSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val unlockManager = UnhookedApp.repository?.emergencyUnlockManager

    var passphraseInput by remember { mutableStateOf("") }
    var verificationState by remember { mutableStateOf<PassphraseState>(PassphraseState.Input) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter Passphrase",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        when (verificationState) {
            is PassphraseState.Input -> {
                Text(
                    text = "Type the emergency passphrase you saved when creating this block. " +
                            "Capitalization doesn't matter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = passphraseInput,
                    onValueChange = {
                        passphraseInput = it
                        errorMessage = ""
                    },
                    label = { Text("Passphrase") },
                    placeholder = { Text("anchor breeze canyon drift ember...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    maxLines = 8,
                    supportingText = {
                        val wordCount = passphraseInput.trim().split(Regex("\\s+"))
                            .filter { it.isNotEmpty() }.size
                        Text("$wordCount words entered")
                    }
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (passphraseInput.isBlank()) {
                            errorMessage = "Please enter the passphrase"
                            return@Button
                        }
                        verificationState = PassphraseState.Verifying
                        scope.launch {
                            val isValid = unlockManager?.verifyPassphrase(
                                scheduleId, passphraseInput
                            ) ?: false
                            if (isValid) {
                                unlockManager?.deactivateUnlock(scheduleId)
                                verificationState = PassphraseState.Success
                            } else {
                                verificationState = PassphraseState.Input
                                errorMessage = "❌ Passphrase doesn't match. Check for typos and try again."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    enabled = passphraseInput.isNotBlank()
                ) {
                    Text("Verify & Unlock", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            is PassphraseState.Verifying -> {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Verifying...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            is PassphraseState.Success -> {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(72.dp),
                    tint = PastelGreen
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "✅ Unlock Successful!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The Admin lock has been removed from this block.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onUnlockSuccess,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        TextButton(onClick = onBack) {
            Text("Cancel", fontSize = 16.sp)
        }
    }
}

private sealed class PassphraseState {
    object Input : PassphraseState()
    object Verifying : PassphraseState()
    object Success : PassphraseState()
}
