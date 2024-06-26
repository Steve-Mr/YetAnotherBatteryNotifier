package com.maary.yetanotherbatterynotifier.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.maary.yetanotherbatterynotifier.R

class QSTileService: TileService() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick() {
        super.onClick()
        val tile = qsTile
        var waitMillis = 500

        while(ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationsPermission()
            Thread.sleep(waitMillis.toLong())
            waitMillis *= 2
        }

        val intent = Intent(this, ForegroundService::class.java)

        if (!ForegroundService.getIsForegroundServiceRunning() && tile.state == Tile.STATE_INACTIVE){
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
            
            applicationContext.startForegroundService(intent)
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.qstile_active)

        }else if (ForegroundService.getIsForegroundServiceRunning() && tile.state == Tile.STATE_ACTIVE){
            applicationContext.stopService(intent)
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.qstile_inactive)
        }
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile

        if (!ForegroundService.getIsForegroundServiceRunning()){
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.qstile_inactive)

        }else{
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.qstile_active)
        }
        tile.updateTile()
    }

    private fun createNotificationChannel(importance:Int ,name:String, descriptionText: String) {
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(name)!=null) return
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestNotificationsPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            startActivityAndCollapse(pendingIntent)
        }
    }
}