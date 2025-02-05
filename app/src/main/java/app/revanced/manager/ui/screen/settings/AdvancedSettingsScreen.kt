package app.revanced.manager.ui.screen.settings

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.viewModelScope
import app.revanced.manager.BuildConfig
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.GroupHeader
import app.revanced.manager.ui.component.settings.BooleanItem
import app.revanced.manager.ui.component.settings.IntegerItem
import app.revanced.manager.ui.component.settings.SafeguardBooleanItem
import app.revanced.manager.ui.component.settings.SettingsListItem
import app.revanced.manager.ui.viewmodel.AdvancedSettingsViewModel
import app.revanced.manager.util.toast
import app.revanced.manager.util.withHapticFeedback
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdvancedSettingsScreen(
    onBackClick: () -> Unit,
    vm: AdvancedSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val memoryLimit = remember {
        val activityManager = context.getSystemService<ActivityManager>()!!
        context.getString(
            R.string.device_memory_limit_format,
            activityManager.memoryClass,
            activityManager.largeMemoryClass
        )
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.advanced),
                scrollBehavior = scrollBehavior,
                onBackClick = onBackClick
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        ColumnWithScrollbar(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GroupHeader(stringResource(R.string.manager))

            val apiUrl by vm.prefs.api.getAsState()
            var showApiUrlDialog by rememberSaveable { mutableStateOf(false) }

            if (showApiUrlDialog) {
                APIUrlDialog(
                    currentUrl = apiUrl,
                    defaultUrl = vm.prefs.api.default,
                    onSubmit = {
                        showApiUrlDialog = false
                        it?.let(vm::setApiSource)
                    }
                )
            }

            GroupHeader(stringResource(R.string.patcher))
            BooleanItem(
                preference = vm.prefs.useProcessRuntime,
                coroutineScope = vm.viewModelScope,
                headline = R.string.process_runtime,
                description = R.string.process_runtime_description,
            )
            IntegerItem(
                preference = vm.prefs.patcherProcessMemoryLimit,
                coroutineScope = vm.viewModelScope,
                headline = R.string.process_runtime_memory_limit,
                description = R.string.process_runtime_memory_limit_description,
            )

            GroupHeader(stringResource(R.string.manager))
            SettingsListItem(
                headlineContent = stringResource(R.string.api_source),
                supportingContent = apiUrl,
                modifier = Modifier.clickable {
                    showApiUrlDialog = true
                },
                trailingContent = {
                    IconButton(onClick = { showApiUrlDialog = true }) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                }
            )
            SafeguardBooleanItem(
                preference = vm.prefs.disablePatchVersionCompatCheck,
                coroutineScope = vm.viewModelScope,
                headline = R.string.allow_compatibility_mixing,
                description = R.string.allow_compatibility_mixing_description,
                confirmationText = R.string.allow_compatibility_mixing_confirmation
            )
            SafeguardBooleanItem(
                preference = vm.prefs.disableUniversalPatchWarning,
                coroutineScope = vm.viewModelScope,
                headline = R.string.universal_patches_safeguard,
                description = R.string.universal_patches_safeguard_description,
                confirmationText = R.string.universal_patches_safeguard_confirmation
            )
            SafeguardBooleanItem(
                preference = vm.prefs.disableSelectionWarning,
                coroutineScope = vm.viewModelScope,
                headline = R.string.patch_selection_safeguard,
                description = R.string.patch_selection_safeguard_description,
                confirmationText = R.string.patch_selection_safeguard_confirmation
            )

            GroupHeader(stringResource(R.string.update))
            BooleanItem(
                preference = vm.prefs.showManagerUpdateDialogOnLaunch,
                headline = R.string.show_manager_update_dialog_on_launch,
                description = R.string.check_for_update_auto_description
            )

            GroupHeader(stringResource(R.string.experimental_features))
            BooleanItem(
                preference = vm.prefs.useProcessRuntime,
                coroutineScope = vm.viewModelScope,
                headline = R.string.process_runtime,
                description = R.string.process_runtime_description,
            )

            GroupHeader(stringResource(R.string.debugging))
            val exportDebugLogsLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
                    it?.let(vm::exportDebugLogs)
                }
            SettingsListItem(
                headlineContent = stringResource(R.string.debug_logs_export),
                modifier = Modifier.clickable { exportDebugLogsLauncher.launch(vm.debugLogFileName) }
            )
            val clipboard = remember { context.getSystemService<ClipboardManager>()!! }
            val deviceContent = """
                    Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                    Build type: ${BuildConfig.BUILD_TYPE}
                    Model: ${Build.MODEL}
                    Android version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})
                    Supported Archs: ${Build.SUPPORTED_ABIS.joinToString(", ")}
                    Memory limit: $memoryLimit
                """.trimIndent()
            SettingsListItem(
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClickLabel = stringResource(R.string.copy_to_clipboard),
                    onLongClick = {
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Device Information", deviceContent)
                        )

                        context.toast(context.getString(R.string.toast_copied_to_clipboard))
                    }.withHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                ),
                headlineContent = stringResource(R.string.about_device),
                supportingContent = deviceContent
            )
        }
    }
}

@Composable
private fun APIUrlDialog(currentUrl: String, defaultUrl: String, onSubmit: (String?) -> Unit) {
    var url by rememberSaveable(currentUrl) { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = { onSubmit(null) },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(url)
                }
            ) {
                Text(stringResource(R.string.api_source_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onSubmit(null) }) {
                Text(stringResource(R.string.cancel))
            }
        },
        icon = {
            Icon(Icons.Outlined.Api, null)
        },
        title = {
            Text(
                text = stringResource(R.string.api_source_dialog_title),
                style = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.api_source_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.api_source_dialog_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.api_source)) },
                    trailingIcon = {
                        IconButton(onClick = { url = defaultUrl }) {
                            Icon(Icons.Outlined.Restore, stringResource(R.string.api_source_dialog_reset))
                        }
                    }
                )
            }
        }
    )
}