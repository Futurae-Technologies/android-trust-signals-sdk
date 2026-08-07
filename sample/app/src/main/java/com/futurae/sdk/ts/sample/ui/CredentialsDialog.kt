package com.futurae.sdk.ts.sample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futurae.sdk.ts.model.public.TSCollectionRequest

private data class AccountEntry(
    val accountId: String = "",
    val accessToken: String = "",
    val appId: String = "",
)

@Composable
fun CredentialsDialog(
    title: String,
    onConfirm: (List<TSCollectionRequest>) -> Unit,
    onDismiss: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null,
) {
    val entries = remember { mutableStateListOf(AccountEntry()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Enter credentials for each account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                entries.forEachIndexed { index, entry ->
                    if (index > 0) HorizontalDivider()
                    AccountEntryFields(
                        index = index,
                        entry = entry,
                        showLabel = entries.size > 1,
                        onEntryChange = { entries[index] = it },
                    )
                }
                TextButton(
                    onClick = { entries.add(AccountEntry()) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("+ Add Account") }
                extraContent?.invoke()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(entries.map {
                        TSCollectionRequest(
                            accountId = it.accountId,
                            accessToken = it.accessToken,
                            appId = it.appId,
                        )
                    })
                },
                enabled = entries.all {
                    it.accountId.isNotBlank() && it.accessToken.isNotBlank() && it.appId.isNotBlank()
                },
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun AccountEntryFields(
    index: Int,
    entry: AccountEntry,
    showLabel: Boolean,
    onEntryChange: (AccountEntry) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showLabel) {
            Text(
                text = "Account ${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        OutlinedTextField(
            value = entry.accountId,
            onValueChange = { onEntryChange(entry.copy(accountId = it)) },
            label = { Text("Account ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = entry.accessToken,
            onValueChange = { onEntryChange(entry.copy(accessToken = it)) },
            label = { Text("Access Token") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )
        OutlinedTextField(
            value = entry.appId,
            onValueChange = { onEntryChange(entry.copy(appId = it)) },
            label = { Text("App ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}
