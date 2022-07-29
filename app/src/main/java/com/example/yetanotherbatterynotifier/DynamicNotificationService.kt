package com.example.yetanotherbatterynotifier

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log
import java.util.*

class DynamicNotificationService: Service() {

    private var timer = Timer()
    private var isTimerRunning = false


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTimerTask()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopTimerTask()
        super.onDestroy()
    }

    private fun startTimerTask() {
        timer = Timer()
        if (!isTimerRunning) {
            isTimerRunning = true
            timer.schedule(object : TimerTask() {
                override fun run() {
                    Log.v("DYNAMIC", "running");
                }
            }, 0, 5000L)
        }
    }

    private fun stopTimerTask() {
        if (isTimerRunning) {
            timer.cancel()
            timer.purge()
            isTimerRunning = false
        }
    }
}