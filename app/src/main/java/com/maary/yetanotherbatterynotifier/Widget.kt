package com.maary.yetanotherbatterynotifier

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.AndroidResourceImageProvider
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver
import com.maary.yetanotherbatterynotifier.service.ForegroundService

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun Content() {

        val fService = remember { ForegroundService.getInstance() }
        val currentNow = fService.currentFlow.collectAsState().value
        val temperatureNow = fService.temperatureFlow.collectAsState().value
        val dndState = fService.dndFlow.collectAsState().value

        val context = LocalContext.current

        val sleepIntent = Intent(context, SettingsReceiver::class.java).apply {
            action = "com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd"
        }

        Column(
            modifier = GlanceModifier
                .cornerRadius(16.dp)
                .clickable(onClick = actionStartActivity<SettingsActivity>())
                .background(GlanceTheme.colors.tertiaryContainer)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Column(
//                modifier = GlanceModifier
//                    .cornerRadius(16.dp)
//                    .fillMaxSize()
//                    .background(GlanceTheme.colors.tertiaryContainer)
//                    .padding(vertical = 4.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
            Column(
                modifier = GlanceModifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.tertiary)
                        .cornerRadius(8.dp)
                        .fillMaxWidth(),
                    text = "$currentNow mA \n $temperatureNow ℃",
                    style = TextStyle(
                        color = GlanceTheme.colors.onTertiary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2,
                )
            }
            Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    text = "    ",
                    onClick = actionSendBroadcast(sleepIntent),
                    enabled = !dndState,
                )
                Image(
                    provider = if (dndState) AndroidResourceImageProvider(R.drawable.ic_notification_off) else AndroidResourceImageProvider(
                        R.drawable.ic_notification_on
                    ),
                    contentDescription = "dnd state",
                    modifier = GlanceModifier.clickable({
                        if (!dndState) actionSendBroadcast(sleepIntent)
                    }),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary)
                )
            }
//            }
        }

    }

}