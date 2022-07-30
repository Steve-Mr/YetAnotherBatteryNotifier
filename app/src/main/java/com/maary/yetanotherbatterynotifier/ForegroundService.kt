package com.maary.yetanotherbatterynotifier

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.os.BatteryManager
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import java.util.*

class ForegroundService : Service() {

    private var timer = Timer()
    private var isTimerRunning = false
    private var isScreenOnReceiver = false
    private var isLevelReceiver = false

    val screenReceiver = ScreenReceiver()
    private val chargingReceiver = ChargingReceiver()
    val levelReceiver = BatteryLevelReceiver()

    lateinit var sharedPref : SharedPreferences

    companion object {
        private var isForegroundServiceRunning = false

        @JvmStatic
        fun isForegroundServiceRunning(): Boolean {
            return isForegroundServiceRunning
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val notification = updateNotificationInfo(
            resources.getString(R.string.default_channel),
            isOnGoing = true,
            isAlertOnce = true,
            title = resources.getString(R.string.yet_another_battery_notifier),
            content = resources.getString(R.string.yet_another_battery_notifier_is_running),
            icon = R.drawable.notification_not_charging,
            withAction = true
        )

        Log.v("STATE", "foreground service")
        startForeground(1, notification)// 2

        val batteryStatus: Intent? = registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL

        val isInteractive = getSystemService<PowerManager>()?.isInteractive

        Log.v("STATE", isCharging.toString())
        Log.v("STATUS", status.toString() + " " + BatteryManager.BATTERY_STATUS_CHARGING.toString())
        if (isCharging && isInteractive == true) {
            startTimerTask()
        }

        isForegroundServiceRunning = true

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        sharedPref = getSharedPreferences(getString(R.string.name_shared_pref), Context.MODE_PRIVATE)
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_POWER_CONNECTED)
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(chargingReceiver, filter)
    }

    override fun onDestroy() {
        Log.v("SERVICE", "onDestroy()")
        isForegroundServiceRunning = false
        unregisterReceiver(chargingReceiver)
        stopTimerTask()
        if (isLevelReceiver) unregisterReceiver(levelReceiver)
        if (isScreenOnReceiver) unregisterReceiver(screenReceiver)
        val notificationManager: NotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        notificationManager.cancel(2)
        super.onDestroy()
    }

    private fun startTimerTask() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timer = Timer()
            val batteryManager: BatteryManager =
                this.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val currentNow: Long =
                        batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    notificationManager.notify(
                        1,
                        updateNotificationInfo(
                            resources.getString(R.string.default_channel),
                            isOnGoing = true,
                            isAlertOnce = true,
                            title = resources.getString(R.string.yet_another_battery_notifier),
                            content = currentNow.toString(),
                            icon = R.drawable.notification_charging,
                            withAction = true
                        )
                    )
                    Log.v("RUNNING", "timer task")
                }
            }, 0, sharedPref.getLong(getString(R.string.shared_pref_frequency), 5000L))
        }
    }

    private fun stopTimerTask() {
        if (isTimerRunning) {
            timer.cancel()
            timer.purge()
            isTimerRunning = false
        }

        val notification = updateNotificationInfo(
            resources.getString(R.string.default_channel),
            isOnGoing = false,
            isAlertOnce = true,
            title = resources.getString(R.string.yet_another_battery_notifier),
            content = resources.getString(R.string.yet_another_battery_notifier_is_running),
            icon = R.drawable.notification_not_charging,
            withAction = true
        )
        val notificationManager: NotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
                Log.v("==SCREENON ALT==", "Screen on")
            } else if ("android.intent.action.SCREEN_OFF" == p1.action) {
                Log.v("==SCREENOFF ALT==", "Screen off")
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

                Log.v("==CHARGING ALT==", "charging")
            } else if ("android.intent.action.ACTION_POWER_DISCONNECTED" == p1.action) {
                Log.v("==DISCHARGED ALT==", "not charging")
                val notificationManager: NotificationManager =
                    p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(2)
                stopTimerTask()
                if (isScreenOnReceiver) {
                    unregisterReceiver(screenReceiver)
                    isScreenOnReceiver = false
                }
                if (isLevelReceiver) {
                    unregisterReceiver(levelReceiver)
                    isLevelReceiver = false
                }

            }
        }
    }

    inner class BatteryLevelReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val level: Int? = p1?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            if (level != null) {
                if (level == 47 || level == 50) {
                    val notificationManager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(
                        2,
                        updateNotificationInfo(
                            resources.getString(R.string.channel_notify),
                            isOnGoing = false,
                            isAlertOnce = false,
                            title = resources.getString(R.string.yet_another_battery_notifier),
                            content = resources.getString(R.string.charged),
                            icon = R.drawable.notification_charging,
                            withAction = false
                        )
                    )
                }
            }
        }
    }

    fun updateNotificationInfo(
        channelId: String,
        isOnGoing: Boolean,
        isAlertOnce: Boolean,
        title: String,
        content: String,
        icon: Int,
        withAction: Boolean
    ): Notification {

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setOngoing(isOnGoing)
            .setSmallIcon(icon)
            .setShowWhen(false)
            .setContentTitle(title)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOnlyAlertOnce(isAlertOnce)
            .setContentText(content)

        if (withAction){
            val settingsIntent = Intent(this, SettingsReceiver::class.java).apply {
                action = "com.maary.yetanotherbatterynotifier.SettingsReceiver"
            }
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE)

            val action : NotificationCompat.Action = NotificationCompat.Action.Builder(
                R.drawable.ic_baseline_settings_24,
                resources.getString(R.string.settings),
                snoozePendingIntent
            ).build()
            notificationBuilder.addAction(action)
        }

        return notificationBuilder.build()

    }
}