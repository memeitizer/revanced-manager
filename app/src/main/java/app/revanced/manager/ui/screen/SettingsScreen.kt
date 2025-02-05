package app.revanced.manager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.revanced.manager.R
import app.revanced.manager.ui.component.AppTopBar
import app.revanced.manager.ui.component.ColumnWithScrollbar
import app.revanced.manager.ui.component.settings.SettingsListItem
import app.revanced.manager.ui.model.navigation.Settings

private val settingsSections = listOf(
    Triple(
        R.string.general,
        R.string.general_description,
        Icons.Outlined.Settings
    ) to Settings.General,
    Triple(
        R.string.extensions,
        R.string.extensions_description,
        Icons.Outlined.Download
    ) to Settings.Downloads,
    Triple(
        R.string.backup_restore,
        R.string.backup_restore_description,
        Icons.Outlined.SwapVert
    ) to Settings.ImportExport,
    Triple(
        R.string.advanced,
        R.string.advanced_description,
        Icons.Outlined.Tune
    ) to Settings.Advanced,
    Triple(
        R.string.about,
        R.string.app_name,
        Icons.Outlined.Info
    ) to Settings.About,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit, navigate: (Settings.Destination) -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings),
                onBackClick = onBackClick,
            )
        }
    ) { paddingValues ->
        ColumnWithScrollbar(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            settingsSections.forEach { (titleDescIcon, destination) ->
                SettingsListItem(
                    modifier = Modifier.clickable { navigate(destination) },
                    headlineContent = stringResource(titleDescIcon.first),
                    supportingContent = stringResource(titleDescIcon.second),
                    leadingContent = { Icon(titleDescIcon.third, null) }
                )
            }
        }
    }
}