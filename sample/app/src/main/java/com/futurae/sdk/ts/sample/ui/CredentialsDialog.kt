package com.futurae.sdk.ts.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CredentialsDialog(
    title: String,
    onConfirm: (accountId: String, accessToken: String) -> Unit,
    onDismiss: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null,
) {
    var accountId by remember { mutableStateOf("") }
    var accessToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Enter your credentials to proceed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Account ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = { Text("Access Token") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                )
                extraContent?.invoke()
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(accountId, accessToken) },
                enabled = accountId.isNotBlank() && accessToken.isNotBlank(),
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
