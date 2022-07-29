package com.example.yetanotherbatterynotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QSTileService: TileService() {

    override fun onClick() {
        super.onClick()
        val tile = qsTile

        val intent = Intent(this, ForegroundService::class.java)

        val chargingReceiver = BatteryChargingReceiver()

        if (!ForegroundService.isForegroundServiceRunning()){
            createNotificationChannel(
                resources.getString(R.string.default_channel),
                resources.getString(R.string.default_channel_description)
            )
            createNotificationChannel(
                resources.getString(R.string.channel_notify),
                resources.getString(R.string.channel_notify_description)
            )

            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_POWER_CONNECTED)
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED)
            registerReceiver(chargingReceiver, filter)

            applicationContext.startForegroundService(intent)
            tile.state = Tile.STATE_ACTIVE

        }else{
            unregisterReceiver(chargingReceiver)
            applicationContext.stopService(intent)
            tile.state = Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }

    private fun createNotificationChannel(name:String, descriptionText: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}