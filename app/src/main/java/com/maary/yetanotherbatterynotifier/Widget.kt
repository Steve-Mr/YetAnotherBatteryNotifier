package com.maary.yetanotherbatterynotifier

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.AndroidResourceImageProvider
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
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
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
    fun DndToggleButtonWidget( // 可以重命名以示区别
        dndState: Boolean,
        sleepIntent: Intent, // 或者 Action
        modifier: GlanceModifier = GlanceModifier
    ) {
        val context = LocalContext.current

        // --- 获取资源 ---
        val contentDescResId = if (dndState) {
            R.string.dnd_content_description_on
        } else {
            R.string.dnd_content_description_off
        }
        val iconResId = if (dndState) {
            R.drawable.ic_notification_off
        } else {
            R.drawable.ic_notification_on
        }
        val contentDescription = context.getString(contentDescResId)

        val buttonBackgroundColor = GlanceTheme.colors.primary
        val iconTintColor = GlanceTheme.colors.onPrimary


        // --- 构建 UI ---
        Box(
            // 外层 Box 控制整体布局和对齐
            modifier = modifier.fillMaxWidth(), // 或者根据需要调整
            contentAlignment = Alignment.Center
        ) {
            // 内层 Box 作为按钮的视觉容器和交互区域
            Box(
                modifier = GlanceModifier
                    .clickable (onClick = actionSendBroadcast(sleepIntent))
                    .background(buttonBackgroundColor)
                    .cornerRadius(64.dp) // 例如 16dp，可以尝试 8.dp, 24.dp, 或 50.dp (接近药丸形状)
                    .padding(horizontal = 20.dp, vertical = 8.dp), // 调整 padding
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = AndroidResourceImageProvider(iconResId),
                    contentDescription = contentDescription,
                    modifier = GlanceModifier,
                    colorFilter = ColorFilter.tint(iconTintColor)
                )
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
            action = "com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd.toggle"
        }

        // 定义基础文本样式，方便复用
        val baseTextStyle = TextStyle(
            color = GlanceTheme.colors.onTertiary,
        )

        // 定义基础字号和较小字号 (可以根据需要调整)
        val defaultFontSize = 14.sp // 示例字号，请根据你的设计调整
        val smallerFontSize = 12.sp // 比默认小 2sp

        Column(
            modifier = GlanceModifier.fillMaxSize()
                .clickable(onClick = actionStartActivity<SettingsActivity>())
                .background(GlanceTheme.colors.tertiaryContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = GlanceModifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.tertiary)
                        .cornerRadius(8.dp).padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally // 使内容在 Column 内水平居中
                ) {
                    // 第一行：电流值 + 单位 (mA)
                    Row(
                        verticalAlignment = Alignment.Bottom // 尝试底部对齐，使不同大小的文字基线对齐效果更好
                    ) {
                        Text(
                            text = "$currentNow ",
                            style = baseTextStyle.copy(fontSize = defaultFontSize) // 使用默认字号
                        )
                        Text(
                            text = "mA",
                            style = baseTextStyle.copy(fontSize = smallerFontSize) // 使用较小字号
                        )
                    }

                    // 第二行：温度值 + 单位 (℃)
                    // Column 会自动处理换行
                    Text(
                        text = "$temperatureNow ℃",
                        style = baseTextStyle.copy(fontSize = defaultFontSize), // 使用默认字号
                        maxLines = 1
                    )
                }
                DndToggleButtonWidget(
                    dndState,
                    sleepIntent,
                    GlanceModifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }

}