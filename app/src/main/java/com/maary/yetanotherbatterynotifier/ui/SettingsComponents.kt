package com.maary.yetanotherbatterynotifier.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maary.yetanotherbatterynotifier.BuildConfig
import com.maary.yetanotherbatterynotifier.R
import com.maary.yetanotherbatterynotifier.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class SettingsComponents {


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
                maxLines = 5
            )
        }
    }

    @Composable
    fun SliderItem(sliderPosition: Float, onValueChange: (Float) -> Unit, onValueChangeFinished: () -> Unit) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = sliderPosition.roundToInt().toString())
            Slider(
                modifier = Modifier.semantics { contentDescription = "Localized Description" },
                value = sliderPosition,
                onValueChange = { onValueChange(it) },
                valueRange = 60f..100f,
                onValueChangeFinished = { onValueChangeFinished() },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropdownItem(modifier: Modifier, options: MutableList<String>, position: Int, onItemClicked: (Int) -> Unit) {
        var expanded by remember {
            mutableStateOf(false)
        }

        Box(modifier = modifier) {
            ExposedDropdownMenuBox(
                modifier =
                Modifier.padding(8.dp),
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .wrapContentWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    value = options[position],//text,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                )
                ExposedDropdownMenu(
                    modifier = Modifier.wrapContentWidth(),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            modifier = Modifier.wrapContentWidth(),
                            text = { Text(option, style = Typography.bodyLarge) },
                            onClick = {
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
            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically){
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
    fun EnableForegroundRow(
        title: String,
        description: String,
        state: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        onNotificationSettingsClicked: () -> Unit
    ) {
        Column {
            SwitchRow(title = title, description = description, state = state, onCheckedChange = onCheckedChange)
            if (state) {
                TextContent(
                    modifier = Modifier
                        .clickable { onNotificationSettingsClicked() }
                        .padding(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 8.dp),
                    title = stringResource(id = R.string.notification_settings),
                    description = stringResource(R.string.notification_settings_description))
            }

        }
    }

    @Composable
    fun AlertPercentRow(title: String, description: String,
                        level1: Float, level2: Float,
                        onLevel1Change: (Float) -> Unit, onLevel1Finished: () -> Unit,
                        onLevel2Change: (Float) -> Unit, onLevel2Finished: () -> Unit) {
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ){
            TextContent(title = title, description = description)
            Row (modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically){
                Text("档位1")
                SliderItem(sliderPosition = level1, onValueChange = onLevel1Change, onValueChangeFinished = onLevel1Finished)
            }
            Row (modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("档位2")
                SliderItem(sliderPosition = level2, onValueChange = onLevel2Change, onValueChangeFinished = onLevel2Finished)
            }
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
            TextContent(modifier = Modifier.weight(3f), title = title, description = description)
            DropdownItem(modifier = Modifier.weight(2f), options = options,
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
            TextContent(modifier = Modifier.weight(1f), title = title, description = description)
            Column(
                modifier =
                Modifier.wrapContentWidth(),
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AboutRow(enableOEM: () -> Unit, disableOEM: () -> Unit) {
        var clickCount by remember {
            mutableIntStateOf(0)
        }
        var job by remember {
            mutableStateOf<Job?>(null)
        }
        Row (modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    clickCount++
                    if (clickCount == 1) {
                        job = CoroutineScope(Dispatchers.Default).launch {
                            delay(5000) // 500 milliseconds
                            withContext(Dispatchers.Main) {
                                clickCount = 0
                                Log.v("SEVM", "test")
                            }
                        }
                    } else if (clickCount == 5) {
                        job?.cancel()
                        clickCount = 0
                        Log.v("SEVM", "true dude")
                        enableOEM()
                    }
                },
                onLongClick = { disableOEM() })
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)){
            TextContent(title = stringResource(id = R.string.app_name), description = BuildConfig.VERSION_NAME)
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
}