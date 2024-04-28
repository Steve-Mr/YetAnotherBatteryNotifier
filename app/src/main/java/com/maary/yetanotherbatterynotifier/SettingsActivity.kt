package com.maary.yetanotherbatterynotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maary.yetanotherbatterynotifier.ui.theme.Typography
import com.maary.yetanotherbatterynotifier.ui.theme.YetAnotherBatteryNotifierTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YetAnotherBatteryNotifierTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen()

                }
            }
        }
    }
}

@Composable
fun TextContent(modifier: Modifier = Modifier, title: String, description: String) {
    Column(modifier = modifier){
        Text(
            title,
            style = Typography.titleLarge
        )
        Text(
            description,
            style = Typography.bodySmall,
            maxLines = 2
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownItem(modifier: Modifier, options: MutableList<String>, position: Int, onItemClicked: (Int) -> Unit) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var text by remember { mutableStateOf(options[position]) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            modifier =
            Modifier
                .padding(8.dp),
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                // The `menuAnchor` modifier must be passed to the text field to handle
                // expanding/collapsing the menu on click. A read-only text field has
                // the anchor type `PrimaryNotEditable`.
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = text,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(
                modifier = Modifier
                    .width(IntrinsicSize.Min),
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .width(IntrinsicSize.Min),
                        text = { Text(option, style = Typography.bodyLarge) },
                        onClick = {
                            text = option
                            expanded = false
                            onItemClicked(options.indexOf(option))
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerItem(title: String, time: Date, onConfirm: (Date) -> Unit) {
    var showTimePicker by remember { mutableStateOf(false) }
    val state = rememberTimePickerState()
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row (modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = true, onClick = { showTimePicker = true })
        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)){
        Text(title)
        Text(modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            text = formatter.format(time))
    }

    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, state.hour)
                cal.set(Calendar.MINUTE, state.minute)
                cal.isLenient = false
                onConfirm(cal.time)
                showTimePicker = false
            },
        ) {
            TimePicker(state = state)
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onCancel
                    ) { Text("Cancel") }
                    TextButton(
                        onClick = onConfirm
                    ) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    description: String,
    state: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextContent(title = title, description = description)
        Switch(checked = state, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DropdownRow(title: String, description: String, options: MutableList<String>, position: Int, onItemClicked: (Int) -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextContent(modifier = Modifier.weight(2f), title = title, description = description)
        DropdownItem(modifier = Modifier.weight(1f), options = options,
            position = position, onItemClicked = onItemClicked)
    }
}

@Composable
fun DNDRow(
    title: String,
    description: String,
    state: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    startTime: Date,
    endTime: Date,
    onStartSet: (Date) -> Unit,
    onEndSet: (Date) -> Unit) {
    Column (
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.Start
    ){
        SwitchRow(title = title, description = description, state = state, onCheckedChange = onCheckedChange)
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)){
            TimePickerItem(title = "开始时间", time = startTime, onConfirm = onStartSet)
            TimePickerItem(title = "结束时间", time = endTime, onConfirm = onEndSet)
        }

    }
}

@Composable
fun FuckOEMRow(title: String, description: String,
               onUpscaleClicked: () -> Unit, onDownScaleClicked: () -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        TextContent(title = title, description = description)
        Column(
            modifier =
            Modifier
                .wrapContentWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(8.dp),
                onClick = onUpscaleClicked) {
                Text("值太小")
            }

            OutlinedButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(8.dp),
                onClick = onDownScaleClicked) {
                Text("值太大")
            }

        }
    }
}

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {

    SettingsTopAppBar { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SwitchRow(
                title = "前台服务",
                description = "显示状态",
                state = settingsViewModel.foregroundSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.foregroundSwitchOnChecked(it) })

            SwitchRow(
                title = "常态显示",
                description = "未充电时也显示状态",
                state = settingsViewModel.alwaysOnSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.alwaysOnSwitchOnChecked(it) })
            DropdownRow(
                title = "更新频率",
                description = "更新速度将按照你的设置进行，分为不同的档位",
                options = SettingsViewModel.FREQUENCY_OPTIONS,
                position = settingsViewModel.frequencyIndexState.collectAsState().value,
                onItemClicked = { settingsViewModel.frequencyItemClicked(it) })
            DNDRow(
                title = "DND 起止时间",
                description = "此时间内不发布通知",
                state = settingsViewModel.dndSwitchState.collectAsState().value,
                onCheckedChange = { settingsViewModel.dndSwitchOnChecked(it) },
                startTime = settingsViewModel.dndStartState.collectAsState().value,
                endTime = settingsViewModel.dndEndState.collectAsState().value,
                onStartSet = { settingsViewModel.setDNDStart(it)},
                onEndSet = { settingsViewModel.setDNDEnd(it)})
            FuckOEMRow(
                title = "干他妈的 OEM",
                description = "适配个 API 都能适配歪来",
                onUpscaleClicked = { settingsViewModel.onUpscaleClicked() },
                onDownScaleClicked = { settingsViewModel.onDownscaleClicked() })
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(content: @Composable (PaddingValues) -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Large Top App Bar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        content(innerPadding)
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