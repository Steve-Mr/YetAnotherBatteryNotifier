package com.maary.yetanotherbatterynotifier.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.maary.yetanotherbatterynotifier.PreferenceRepository
import com.maary.yetanotherbatterynotifier.R
import com.maary.yetanotherbatterynotifier.SettingsActivity
import com.maary.yetanotherbatterynotifier.Widget
import com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundService : LifecycleService() {

    private var timer: Timer? = null
    private var isScreenOnReceiver = false
    private var isLevelReceiver = false

    @Inject
    lateinit var preferences: PreferenceRepository

    private var level1 = 80
    private var level2 = 85
    private var negativeIsCharging = true

    val screenReceiver = ScreenReceiver()
    private val chargingReceiver = ChargingReceiver()
    val levelReceiver = BatteryLevelReceiver()

    private val _currentFlow = MutableStateFlow(0L)
    val currentFlow: StateFlow<Long> = _currentFlow

    private val _temperatureFlow = MutableStateFlow(0)
    val temperatureFlow: StateFlow<Int> = _temperatureFlow

    private val _dndState = MutableStateFlow(false)
    val dndFlow: StateFlow<Boolean> = _dndState

    companion object {
        private val _isForegroundServiceRunning = MutableStateFlow(false)
        val isForegroundServiceRunning: StateFlow<Boolean>
            get() = _isForegroundServiceRunning

        @JvmStatic
        fun getIsForegroundServiceRunning(): Boolean {
            return _isForegroundServiceRunning.value
        }

        private var instance: ForegroundService? = null

        @JvmStatic
        fun getInstance(): ForegroundService {
            return instance
                ?: throw IllegalStateException("ForegroundService instance has not been initialized.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = updateNotificationInfo(
            resources.getString(R.string.default_channel),
            isOnGoing = true,
            isAlertOnce = true,
            title = "",//resources.getString(R.string.yet_another_battery_notifier),
            content = resources.getString(R.string.yet_another_battery_notifier_is_running),
            icon = R.drawable.notification_not_charging,
            withAction = true,
            priority = NotificationCompat.PRIORITY_MIN
        )

        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)// 2

        val batteryStatus: Intent? = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        val isInteractive = getSystemService<PowerManager>()?.isInteractive

        if (isCharging && isInteractive == true) {
            startTimerTask()
        }

        _isForegroundServiceRunning.value = true

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        preferences.getLevel1().onEach {
            level1 = it
        }.launchIn(lifecycleScope)
        preferences.getLevel2().onEach {
            level2 = it
        }.launchIn(lifecycleScope)
        preferences.getNegativeIsCharging().onEach {
            negativeIsCharging = it
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            var filter = IntentFilter()
            if (preferences.getAlwaysShow().first()) {
                if (!isScreenOnReceiver) {
                    filter.addAction(Intent.ACTION_SCREEN_ON)
                    filter.addAction(Intent.ACTION_SCREEN_OFF)
                    registerReceiver(screenReceiver, filter)
                    isScreenOnReceiver = true
                    filter = IntentFilter()
                }
                if (getSystemService<PowerManager>()?.isInteractive == true) {
                    startTimerTask()
                }
            }
            filter.addAction(Intent.ACTION_POWER_CONNECTED)
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
            registerReceiver(chargingReceiver, filter)
        }

        instance = this
    }

    override fun onDestroy() {
        _isForegroundServiceRunning.value = false
        unregisterReceiver(chargingReceiver)
        stopTimerTask()
        if (isLevelReceiver) unregisterReceiver(levelReceiver)
        if (isScreenOnReceiver) unregisterReceiver(screenReceiver)
        val notificationManager: NotificationManager =
            this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        notificationManager.cancel(2)
        instance = null
        super.onDestroy()
    }

    private fun startTimerTask() {

        var ratio = 1000
        var frequency = 5000L
        preferences.getRatio().onEach {
            ratio = it
        }.launchIn(lifecycleScope)
        preferences.getFrequency().onEach {
            frequency = it
        }.launchIn(lifecycleScope)
        if (timer == null) {
            timer = Timer()
            val batteryManager: BatteryManager =
                getSystemService(BATTERY_SERVICE) as BatteryManager
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            timer?.schedule(object : TimerTask() {
                override fun run() {
                    val batteryStatus: Intent? = registerReceiver(
                        null,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    )

                    var currentNow: Long =
                        batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                            .div(ratio)
                    if (negativeIsCharging) {
                        currentNow = -currentNow
                    }

                    _currentFlow.value = currentNow
                    _temperatureFlow.value =
                        (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                            ?.div(10) ?: 0)

                    notificationManager.notify(
                        1,
                        updateNotificationInfo(
                            resources.getString(R.string.default_channel),
                            isOnGoing = true,
                            isAlertOnce = true,
                            title = "",//resources.getString(R.string.yet_another_battery_notifier),
                            content = getString(
                                R.string.string_monitoring, currentNow.toString(),
                                (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                                    ?.div(10) ?: 0).toString()
                            ),
                            icon = R.drawable.notification_charging,
                            withAction = true,
                            priority = NotificationCompat.PRIORITY_MIN
                        )
                    )

                    lifecycleScope.launch {
                        Widget().updateAll(applicationContext)
                    }
                    Log.v("RUNNING", "timer task")
                }
            }, 0, frequency)
        }


    }

    private fun stopTimerTask() {
        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }

        val notification = updateNotificationInfo(
            resources.getString(R.string.default_channel),
            isOnGoing = true,
            isAlertOnce = true,
            title = "",//resources.getString(R.string.yet_another_battery_notifier),
            content = resources.getString(R.string.yet_another_battery_notifier_is_running),
            icon = R.drawable.notification_not_charging,
            withAction = true,
            priority = NotificationCompat.PRIORITY_MIN
        )
        val notificationManager: NotificationManager =
            this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (
                ("android.intent.action.SCREEN_ON" != p1?.action
                        && "android.intent.action.SCREEN_OFF" != p1?.action)
                || p0?.applicationContext == null
            ) {
                return
            }
            if ("android.intent.action.SCREEN_ON" == p1.action) {
                startTimerTask()
            } else if ("android.intent.action.SCREEN_OFF" == p1.action) {
                stopTimerTask()
            }
        }
    }

    inner class ChargingReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (
                ("android.intent.action.ACTION_POWER_CONNECTED" != p1?.action
                        && "android.intent.action.ACTION_POWER_DISCONNECTED" != p1?.action)
                || p0?.applicationContext == null
            ) {
                return
            }
            if ("android.intent.action.ACTION_POWER_CONNECTED" == p1.action) {

                if (getSystemService<PowerManager>()?.isInteractive != false) {
                    startTimerTask()
                }
                if (!isScreenOnReceiver) {
                    val filter = IntentFilter()
                    filter.addAction(Intent.ACTION_SCREEN_ON)
                    filter.addAction(Intent.ACTION_SCREEN_OFF)
                    registerReceiver(screenReceiver, filter)
                    isScreenOnReceiver = true
                }
                if (!isLevelReceiver) {
                    registerReceiver(levelReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    isLevelReceiver = true
                }
            } else if ("android.intent.action.ACTION_POWER_DISCONNECTED" == p1.action) {
                val notificationManager: NotificationManager =
                    p0.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(2)
                if (isLevelReceiver) {
                    unregisterReceiver(levelReceiver)
                    isLevelReceiver = false
                }
                preferences.getAlwaysShow().onEach {
                    if (!it) {
                        stopTimerTask()
                        if (isScreenOnReceiver) {
                            unregisterReceiver(screenReceiver)
                            isScreenOnReceiver = false
                        }
                    }
                }.launchIn(lifecycleScope)
            }
        }
    }

    inner class BatteryLevelReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val currentTime =
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance()
                    .get(Calendar.MINUTE)
            val level: Int? = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)

            runBlocking {
                if (preferences.getDndSet().first() || preferences.getTempDnd().first()) {
                    val startTime = calculateMinutesFromDate(
                        preferences.getDndStartTime().first()!!
                    )
                    val endTime = calculateMinutesFromDate(
                        preferences.getDndEndTime().first()!!
                    )

                    val isNightTime = if (endTime < startTime) {
                        currentTime >= startTime || currentTime <= endTime
                    } else {
                        currentTime in startTime..endTime
                    }

                    val isTempDnd =
                        System.currentTimeMillis()
                            .minus(preferences.getTempDndEnabledTime().first()) < 1000 * 60 * 60

                    if (isNightTime || isTempDnd) {
                        Log.v("YABN_DEBUG", "Notification BLOCKED by DND logic")
                        return@runBlocking
                    }
                }

                if (level != null) {
                    if (level == level1 || level == level2) {

                        val notificationManager: NotificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            2,
                            updateNotificationInfo(
                                resources.getString(R.string.channel_notify),
                                isOnGoing = false,
                                isAlertOnce = false,
                                title = resources.getString(R.string.charged, level, "%"),
                                content = "",
                                icon = R.drawable.notification_charging,
                                withAction = false,
                                priority = NotificationCompat.PRIORITY_DEFAULT
                            )
                        )
                    }
                }
            }
        }

        private fun calculateMinutesFromDate(date: Date): Int {
            val calendar = Calendar.getInstance().apply {
                time = date
            }
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            return hours * 60 + minutes
        }
    }

    fun updateNotificationInfo(
        channelId: String,
        isOnGoing: Boolean,
        isAlertOnce: Boolean,
        title: String,
        content: String,
        icon: Int,
        withAction: Boolean,
        priority: Int
    ): Notification {

        var dndTitleResource = R.string.dnd_1hour
        var contentFinal = content

        runBlocking {
            if (preferences.getTempDnd().first()) {
                val dndSetTime: Long = preferences.getTempDndEnabledTime().first()
                if ((System.currentTimeMillis() - dndSetTime) < 1000 * 60 * 60) {
                    dndTitleResource = R.string.dnd_ing
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = dndSetTime
                    }
                    val h = "%02d".format(calendar.get(Calendar.HOUR_OF_DAY) + 1)
                    val m = "%02d".format(calendar.get(Calendar.MINUTE))
                    contentFinal += getString(R.string.notification_dnd_info_end, "$h:$m")
                    _dndState.value = true
                } else {
                    preferences.setTempDnd(false)
                    _dndState.value = false
                }
            } else if (preferences.getDndSet().first()) {
                val currentTime = Calendar.getInstance().apply {
                    time = Date()
                }
                val startTime = Calendar.getInstance().apply {
                    time = preferences.getDndStartTime().first()!!
                }
                val endTime = Calendar.getInstance().apply {
                    time = preferences.getDndEndTime().first()!!
                }

                val currentMinutes =
                    currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)
                val startMinutes =
                    startTime.get(Calendar.MINUTE) + startTime.get(Calendar.HOUR_OF_DAY) * 60
                val endMinutes =
                    endTime.get(Calendar.MINUTE) + endTime.get(Calendar.HOUR_OF_DAY) * 60

                val sh = "%02d".format(startMinutes / 60)
                val sm = "%02d".format((startMinutes) % 60)

                val eh = "%02d".format(endMinutes / 60)
                val em = "%02d".format((endMinutes) % 60)

                if (endMinutes < startMinutes) {
                    if (currentMinutes >= startMinutes || currentMinutes <= endMinutes) {
                        dndTitleResource = R.string.dnd_ing
                        contentFinal += getString(R.string.notification_dnd_info_end, "$eh:$em")
                        _dndState.value = true
                    } else {
                        contentFinal += getString(R.string.notification_dnd_info_start, "$sh:$sm")
                        _dndState.value = false
                    }
                } else if (currentMinutes in startMinutes..endMinutes) {
                    dndTitleResource = R.string.dnd_ing
                    contentFinal += getString(R.string.notification_dnd_info_end, "$eh:$em")
                    _dndState.value = true
                } else {
                    contentFinal += getString(R.string.notification_dnd_info_start, "$sh:$sm")
                    _dndState.value = false
                }
            }
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setOngoing(isOnGoing)
            .setSmallIcon(icon)
            .setShowWhen(false)
            .setContentTitle(title)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOnlyAlertOnce(isAlertOnce)
            .setContentText(contentFinal)
            .setPriority(priority)

        if (withAction) {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            val settingsPendingIntent =
                PendingIntent.getActivity(this, 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE)

            val actionSettings: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_settings_24,
                resources.getString(R.string.settings),
                settingsPendingIntent
            ).build()

            notificationBuilder.setContentIntent(settingsPendingIntent)

            val sleepIntent = Intent(this, SettingsReceiver::class.java).apply {
                action = "com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd.toggle"
            }

            val sleepPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, sleepIntent, PendingIntent.FLAG_IMMUTABLE)

            val actionSleep: NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_dnd,
                resources.getString(dndTitleResource),
                sleepPendingIntent
            ).build()


            notificationBuilder.addAction(actionSettings)
            notificationBuilder.addAction(actionSleep)
        }

        return notificationBuilder.build()

    }
}
