package com.maary.yetanotherbatterynotifier.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maary.yetanotherbatterynotifier.service.ForegroundService

class BootCompleteReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(p0: Context?, p1: Intent?) {
        val intent = Intent(p0, ForegroundService::class.java)
        p0?.startForegroundService(intent)
    }
}