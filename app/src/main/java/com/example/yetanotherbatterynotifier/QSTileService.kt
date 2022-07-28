package com.example.yetanotherbatterynotifier

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QSTileService: TileService() {
    var isClicked = false

    override fun onClick() {
        super.onClick()
        val tile = qsTile

        val intent = Intent(this, ForegroundService::class.java)

        if (!ForegroundService.isForegroundServiceRunning()){
            applicationContext.startForegroundService(intent)
            tile.state = Tile.STATE_ACTIVE
            isClicked = true
        }else{
            applicationContext.stopService(intent)
            tile.state = Tile.STATE_INACTIVE
            isClicked = false
        }
        tile.updateTile()
    }
}