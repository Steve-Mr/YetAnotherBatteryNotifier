package com.maary.yetanotherbatterynotifier.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.maary.yetanotherbatterynotifier.SettingsViewModel
import com.maary.yetanotherbatterynotifier.ui.theme.YetAnotherBatteryNotifierTheme

import com.maary.yetanotherbatterynotifier.ui.SettingsComponents


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

        LaunchedEffect(notificationPermissionState) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val context = LocalContext.current


    SettingsComponents().SettingsTopAppBar { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            SettingsComponents().EnableForegroundRow(
                title = "前台服务",
                description = "显示状态",
                state = settingsViewModel.foregroundSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.foregroundSwitchOnChecked(it) },
                onNotificationSettingsClicked = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                })
            SettingsComponents().AlertPercentRow(
                title = "提醒档位1",
                description = "充电至该水平将发出通知",
                level1 = settingsViewModel.notifyLevel1State.collectAsState().value,
                onLevel1Change = { settingsViewModel.onLevel1Changed(it) },
                onLevel1Finished = { settingsViewModel.onLevel1Finished() },
                level2 = settingsViewModel.notifyLevel2State.collectAsState().value,
                onLevel2Change = { settingsViewModel.onLevel2Changed(it) },
                onLevel2Finished = { settingsViewModel.onLevel2Finished() },
            )
            SettingsComponents().SwitchRow(
                title = "常态显示",
                description = "未充电时也显示状态",
                state = settingsViewModel.alwaysOnSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.alwaysOnSwitchOnChecked(it) })
            SettingsComponents().DropdownRow(
                title = "更新频率",
                description = "更新速度将按照你的设置进行，分为不同的档位",
                options = SettingsViewModel.FREQUENCY_OPTIONS,
                position = settingsViewModel.frequencyIndexState.collectAsState().value,
                onItemClicked = { settingsViewModel.frequencyItemClicked(it) })
            SettingsComponents().DNDRow(
                title = "DND 起止时间",
                description = "此时间内不发布通知",
                state = settingsViewModel.dndSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.dndSwitchOnChecked(it) },
                startTime = settingsViewModel.dndStartState.collectAsState().value,
                endTime = settingsViewModel.dndEndState.collectAsState().value,
                onStartSet = { settingsViewModel.setDNDStart(it)},
                onEndSet = { settingsViewModel.setDNDEnd(it)})
            SettingsComponents().FuckOEMRow(
                title = stringResource(id = settingsViewModel.oemTitle.collectAsState().value),
                description = stringResource(id = settingsViewModel.oemDescription.collectAsState().value),
                onUpscaleClicked = { settingsViewModel.onUpscaleClicked() },
                onDownScaleClicked = { settingsViewModel.onDownscaleClicked() })
            SettingsComponents().AboutRow(
                enableOEM = { settingsViewModel.enableOEMLabel() },
                disableOEM = { settingsViewModel.restoreOEMLabel() }
            )
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.systemBars
                )
            )
        }
    }
}




//@Preview(showSystemUi = true)
@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    YetAnotherBatteryNotifierTheme {
        SettingsScreen()
    }
}