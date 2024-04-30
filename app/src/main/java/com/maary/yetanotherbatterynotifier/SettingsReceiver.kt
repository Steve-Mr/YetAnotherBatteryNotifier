package com.maary.yetanotherbatterynotifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsReceiver : BroadcastReceiver() {
    @Inject lateinit var preferences: PreferenceRepository
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.v("SETTINGS", "received")

        if ("com.maary.yetanotherbatterynotifier.SettingsReceiver.dnd" == p1?.action) {
            CoroutineScope(Dispatchers.IO).launch {
                preferences.setTempDnd(true)
                preferences.setTempDndEnabledTime(System.currentTimeMillis())
            }
        }
    }
}