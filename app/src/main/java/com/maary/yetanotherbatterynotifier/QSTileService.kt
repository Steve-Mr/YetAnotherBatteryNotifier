package com.maary.yetanotherbatterynotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class QSTileService: TileService() {

    override fun onClick() {
        super.onClick()
        val tile = qsTile

        val intent = Intent(this, ForegroundService::class.java)

        if (!ForegroundService.isForegroundServiceRunning()){
            createNotificationChannel(
                NotificationManager.IMPORTANCE_MIN,
                resources.getString(R.string.default_channel),
                resources.getString(R.string.default_channel_description)
            )
            createNotificationChannel(
                NotificationManager.IMPORTANCE_DEFAULT,
                resources.getString(R.string.channel_notify),
                resources.getString(R.string.channel_notify_description)
            )
            createNotificationChannel(
                NotificationManager.IMPORTANCE_LOW,
                resources.getString(R.string.channel_settings),
                resources.getString(R.string.channel_settings_description)
            )
            
            applicationContext.startForegroundService(intent)
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.qstile_active)

        }else{
            Log.v("QST", "trying to stop service")
            applicationContext.stopService(intent)
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.qstile_inactive)
        }
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile

        if (!ForegroundService.isForegroundServiceRunning()){
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.qstile_inactive)

        }else{
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.qstile_active)
        }
        tile.updateTile()
    }

    private fun createNotificationChannel(importance:Int ,name:String, descriptionText: String) {
        //val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}