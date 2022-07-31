package com.maary.yetanotherbatterynotifier

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class SettingsReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.v("SETTINGS", "received")

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver" == p1?.action) {

            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE)

            val alwaysShowSpeed =
                sharedPref?.getBoolean(p0.getString(R.string.boolean_always_show_speed), false)

            var actionChargingTextR = R.string.always_show_current
            if (alwaysShowSpeed == true){
                actionChargingTextR = R.string.only_charging_current
            }

            val actionCharging = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.NotCharging",
                    actionChargingTextR
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

            val actionCancel = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Cancel",
                    R.string.cancel
                )
            }

            val actions: MutableList<NotificationCompat.Action> = ArrayList()
            actionCharging?.let { actions.add(it) }
            actionFrequency?.let { actions.add(it) }
            actionCancel?.let { actions.add(it) }
//            actionAutoBoot?.let { actions.add(it) }

            p0?.let { notify(it,actions) }

        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.NotCharging" == p1?.action) {
            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE)?: return
            val alwaysShowSpeedValue =  sharedPref?.getBoolean(p0.getString(R.string.boolean_always_show_speed), false)
            with(sharedPref.edit()){
                if (alwaysShowSpeedValue != null) {
                    putBoolean(p0.getString(R.string.boolean_always_show_speed), !alwaysShowSpeedValue)
                }
                apply()
                val notificationManager: NotificationManager =
                    p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(3)
                Toast.makeText(p0,p0.getString(R.string.need_restart_service), Toast.LENGTH_SHORT).show()
            }
            Log.v("SETTINGS", "show not charging ")
        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency" == p1?.action) {

            val actionOneSec = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.OneSec",
                    R.string.one_sec
                )
            }
            val actionTwoSec = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.TwoSec",
                    R.string.two_sec
                )
            }
            val actionFiveSec = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.FiveSec",
                    R.string.five_sec
                )
            }
            val actionOneMin = p0?.let {
                generateAction(
                    it,
                    SettingsReceiver::class.java,
                    "com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.OneMin",
                    R.string.one_min
                )
            }
            val actions: MutableList<NotificationCompat.Action> = ArrayList()
            actionOneSec?.let { actions.add(it) }
            actionTwoSec?.let { actions.add(it) }
            actionFiveSec?.let { actions.add(it) }
            actionOneMin?.let { actions.add(it) }

            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE)?: return

            val sharedPrefFrequency = sharedPref.getLong(p0.getString(R.string.shared_pref_frequency), 5000L)
            if (sharedPrefFrequency == 1000L){
                actions.removeAt(0)
            }
            if (sharedPrefFrequency == 2000L){
                actions.removeAt(1)
            }
            if (sharedPrefFrequency == 5000L){
                actions.removeAt(2)
            }
            if (sharedPrefFrequency == 60000L){
                actions.removeAt(3)
            }
            notify(p0,actions)

        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.AutoBoot" == p1?.action) {
            Log.v("SETTINGS", "auto boot")
            // todo; 写入 qstileservice 查询
        }

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.OneSec" == p1?.action){
            Log.v("ONE SEC", "ONE SEC")
            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()){
                putLong(p0.getString(R.string.shared_pref_frequency), 1000L)
                apply()
            }
            val notificationManager: NotificationManager =
                p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(3)
            Toast.makeText(p0,p0.getString(R.string.need_restart_service), Toast.LENGTH_SHORT).show()

        }
        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.TwoSec" == p1?.action){
//            Log.v("ONE SEC", " SEC")
            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()){
                putLong(p0.getString(R.string.shared_pref_frequency), 2000L)
                apply()
            }
            val notificationManager: NotificationManager =
                p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(3)
            Toast.makeText(p0,p0.getString(R.string.need_restart_service), Toast.LENGTH_SHORT).show()

        }
        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.FiveSec" == p1?.action){
//            Log.v("ONE SEC", "ONE SEC")
            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()){
                putLong(p0.getString(R.string.shared_pref_frequency), 5000L)
                apply()
            }
            val notificationManager: NotificationManager =
                p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(3)
            Toast.makeText(p0,p0.getString(R.string.need_restart_service), Toast.LENGTH_SHORT).show()

        }
        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Frequency.OneMin" == p1?.action){
//            Log.v("ONE SEC", "ONE SEC")
            val sharedPref =
                p0?.getSharedPreferences(p0.getString(R.string.name_shared_pref), Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()){
                putLong(p0.getString(R.string.shared_pref_frequency), 60000L)
                apply()
            }
            val notificationManager: NotificationManager =
                p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(3)
            Toast.makeText(p0,p0.getString(R.string.need_restart_service), Toast.LENGTH_SHORT).show()

        }
        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.Cancel" == p1?.action){
            val notificationManager: NotificationManager =
                p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(3)

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

    private fun notify(
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