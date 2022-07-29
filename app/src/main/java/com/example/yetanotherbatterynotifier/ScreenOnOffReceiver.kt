package com.example.yetanotherbatterynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenOnOffReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (
            ("android.intent.action.SCREEN_ON" != p1?.action
                    && "android.intent.action.SCREEN_OFF" != p1?.action)
            || p0?.applicationContext == null
        ) {
            return
        }
        val intent = Intent(p0, DynamicNotificationService::class.java)
        if ("android.intent.action.SCREEN_ON" == p1.action) {
            p0.startService(intent)
            Log.v("==SCREENON ALT==", "Screen on")
        } else if ("android.intent.action.SCREEN_OFF" == p1.action) {
            Log.v("==SCREENOFF ALT==", "Screen off")
            p0.stopService(intent)
        }
    }
}