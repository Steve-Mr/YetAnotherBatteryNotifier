package com.maary.yetanotherbatterynotifier

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver
import com.maary.yetanotherbatterynotifier.service.ForegroundService

class Widget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @Composable
    fun Content() {

        val fService = remember { ForegroundService.getInstance() }
        val currentNow = fService.currentFlow.collectAsState().value
        val temperatureNow = fService.temperatureFlow.collectAsState().value

        val sleepIntent = Intent(LocalContext.current, SettingsReceiver::class.java).apply {
            action = "com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd"
        }

        Column (
            modifier = GlanceModifier.fillMaxSize()
                .background(GlanceTheme.colors.tertiaryContainer)
                .clickable(onClick = actionStartActivity<SettingsActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                modifier = GlanceModifier
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                text = "$currentNow mA",
                style = TextStyle(
                    color = GlanceTheme.colors.onTertiaryContainer,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
            )
            Text(
                modifier = GlanceModifier.padding(8.dp),
                text = "$temperatureNow ℃",
                style = TextStyle(
                    color = GlanceTheme.colors.onTertiaryContainer,
                    textAlign = TextAlign.End
                ),
                maxLines = 1
            )
            Button(
                text = LocalContext.current.getString(R.string.dnd),
                onClick = actionSendBroadcast(sleepIntent)
            )
        }
    }

}