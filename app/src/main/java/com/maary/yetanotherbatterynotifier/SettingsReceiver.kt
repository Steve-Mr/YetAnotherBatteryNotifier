package com.maary.yetanotherbatterynotifier

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Collections.frequency

class SettingsReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.v("SETTINGS", "received")

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver" == p1?.action) {

            val actionCharging = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.NotCharging",
                    R.string.always_show_current
                    )
            }

            val actionFrequency = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency",
                    R.string.frequency
                )
            }


            val actionAutoBoot = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.AutoBoot",
                    R.string.disable_auto_boot
                )
            }

            val actions: MutableList<NotificationCompat.Action> = ArrayList()
            actionCharging?.let { actions.add(it) }
            actionFrequency?.let { actions.add(it) }
            actionAutoBoot?.let { actions.add(it) }

            p0?.let { notify(it,actions) }

        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.NotCharging" == p1?.action) {
            // TODO: 写入值，foreground receiver
            Log.v("SETTINGS", "show not charging ")
        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency" == p1?.action) {
            // todo: 写入，foreground 查询
        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.AutoBoot" == p1?.action) {
            Log.v("SETTINGS", "auto boot")
            // todo; 写入 qstileservice 查询
        }

        }

    private fun generateAction(
        context: Context,
        targetClass: Class<*>,
        actionName: String,
        actionText: Int

    ): NotificationCompat.Action {
        val intent = Intent(context, targetClass).apply {
            action = actionName
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_settings_24,
            context.getString(actionText),
            pendingIntent
        ).build()
    }

    fun notify(
        context: Context,
        actions: List<NotificationCompat.Action>){

        val notificationSettings = context.let {
            NotificationCompat.Builder(it, context.resources?.getString(R.string.channel_settings)!!)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_settings_24)
                .setShowWhen(false)
                .setContentTitle(context.resources?.getString(R.string.YABN_settings))
                .setOnlyAlertOnce(true)
        }

        for (action in actions){
            notificationSettings.addAction(action)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(3, notificationSettings.build())
    }

    }