package com.example.tanuki.feature.library

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.tanuki.R
import com.example.tanuki.app.TanukiApplication
import com.example.tanuki.core.storage.SafStorageManager
import com.example.tanuki.core.storage.StorageSelectionResult
import com.example.tanuki.core.ui.theme.Dimens

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    storageManager: SafStorageManager? = null
) {
    val context = LocalContext.current
    val manager = storageManager ?: remember(context) {
        (context.applicationContext as? TanukiApplication)?.container?.storageManager
            ?: SafStorageManager(context)
    }

    var rootUriString by remember { mutableStateOf(manager.getSavedRootUriString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val defaultPermissionError = stringResource(R.string.storage_error_permission)

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uri: Uri? = intent?.data
            val flags = intent?.flags ?: 0
            if (uri != null) {
                when (val selectionResult = manager.saveStorageRoot(uri, flags)) {
                    is StorageSelectionResult.Success -> {
                        rootUriString = selectionResult.uri.toString()
                        errorMessage = null
                    }
                    is StorageSelectionResult.Failure -> {
                        errorMessage = selectionResult.message
                    }
                    StorageSelectionResult.Cancelled -> {
                        // Selection cancelled: retain existing selection
                    }
                }
            } else {
                errorMessage = defaultPermissionError
            }
        }
    }

    val openFolderPicker = {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        safLauncher.launch(intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.storage_setup_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.small))

                Text(
                    text = stringResource(R.string.storage_setup_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.medium))

                if (rootUriString != null) {
                    Text(
                        text = stringResource(R.string.storage_status_configured),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.extraSmall))
                    Text(
                        text = rootUriString.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.medium))
                    OutlinedButton(onClick = openFolderPicker) {
                        Text(stringResource(R.string.storage_change_button))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.storage_status_unconfigured),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Dimens.medium))
                    Button(onClick = openFolderPicker) {
                        Text(stringResource(R.string.storage_select_button))
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.small))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
