package com.example.yetanotherbatterynotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel(
            resources.getString(R.string.default_channel),
            resources.getString(R.string.default_channel_description)
        )
        createNotificationChannel(
            resources.getString(R.string.channel_notify),
            resources.getString(R.string.channel_notify_description)
        )

        val intent = Intent(this, ForegroundService::class.java)
        applicationContext.startForegroundService(intent)
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