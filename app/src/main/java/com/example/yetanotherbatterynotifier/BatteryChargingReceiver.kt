package com.example.yetanotherbatterynotifier

import android.content.*
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService

class BatteryChargingReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.v("intent action ", p1?.action.toString())
        if (
            ("android.intent.action.ACTION_POWER_CONNECTED" != p1?.action
                    && "android.intent.action.ACTION_POWER_DISCONNECTED" != p1?.action)
            || p0?.applicationContext == null
        ) {
            return
        }

        val intent = Intent(p0, DynamicNotificationService::class.java)
        val filter = IntentFilter()

        val screenReceiver = ScreenOnOffReceiver()

        if ("android.intent.action.ACTION_POWER_CONNECTED" == p1.action) {

            if (p0.getSystemService<PowerManager>()?.isInteractive != false) {
                p0.startService(intent)
            }

            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            p0.registerReceiver(screenReceiver, filter)
            Log.v("==CHARGING ALT==", "Screen on")
        }
        if ("android.intent.action.ACTION_POWER_DISCONNECTED" == p1.action) {
            Log.v("==DISCHARGED ALT==", "from receiver")
            p0.unregisterReceiver(screenReceiver)

            p0.stopService(intent)
        }
    }
}