package com.maary.yetanotherbatterynotifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maary.yetanotherbatterynotifier.PreferenceRepository
import com.maary.yetanotherbatterynotifier.service.ForegroundService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsReceiver : BroadcastReceiver() {
    @Inject lateinit var preferences: PreferenceRepository
    override fun onReceive(p0: Context?, p1: Intent?) {
        if ("com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd" == p1?.action) {
            CoroutineScope(Dispatchers.IO).launch {
                preferences.setTempDnd(true)
                preferences.setTempDndEnabledTime(System.currentTimeMillis())
                val intent = Intent(p0, ForegroundService::class.java)
                p0?.stopService(intent)
                delay(100)
                p0?.startForegroundService(intent)
            }
        }
        if ("com.maary.yetanotherbatterynotifier.receiver.SettingsReceiver.dnd.toggle" == p1?.action) {
            CoroutineScope(Dispatchers.IO).launch {
                preferences.toggleTempDnd()
                val intent = Intent(p0, ForegroundService::class.java)
                p0?.stopService(intent)
                delay(100)
                p0?.startForegroundService(intent)
            }
        }
    }
}