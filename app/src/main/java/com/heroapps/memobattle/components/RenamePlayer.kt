package com.heroapps.memobattle.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heroapps.memobattle.R

/**
 * Created by h.laroussi on 11/05/2025
 */

@Composable
fun RenamePlayerDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit
) {
    var newName by rememberSaveable { mutableStateOf(defaultName) }

    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.rename_player)) },
        text = {
            OutlinedTextField(
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                value = newName,
                onValueChange = {
                    newName = it
                }
            )
        },
        confirmButton = {
            Button(onClick = {
                onNameChanged(newName)
            }) {
                Text(stringResource(R.string.rename))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}