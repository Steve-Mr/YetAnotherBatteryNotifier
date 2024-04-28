package com.maary.yetanotherbatterynotifier

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date

class SettingsViewModel: ViewModel() {

    companion object {
        val FREQUENCY_OPTIONS = mutableListOf("1s", "2s", "5s", "1min")
    }

    /**
     * 是否启用前台服务
     * */
    private val _foregroundSwitchState = MutableStateFlow(false)
    val foregroundSwitchState: StateFlow<Boolean> = _foregroundSwitchState.asStateFlow()

    fun foregroundSwitchOnChecked(state: Boolean) {
        _foregroundSwitchState.value = !_foregroundSwitchState.value
    }

    /**
     * 是否启用常态显示
     * */
    private val _alwaysOnSwitchState = MutableStateFlow(false)
    val alwaysOnSwitchState: StateFlow<Boolean> = _alwaysOnSwitchState.asStateFlow()

    fun alwaysOnSwitchOnChecked(state: Boolean) {
        _alwaysOnSwitchState.value = !_alwaysOnSwitchState.value
    }


    /**
     * 更新频率
     * */
    private val _frequencyIndexState = MutableStateFlow(0)
    val frequencyIndexState = _frequencyIndexState.asStateFlow()

    fun frequencyItemClicked(index: Int) {
        _frequencyIndexState.value = index
        Log.v("SEVM", "$index")
    }

    /**
     * 是否启用 DND
     * */
    private val _dndSwitchState = MutableStateFlow(false)
    val dndSwitchState: StateFlow<Boolean> = _dndSwitchState.asStateFlow()

    fun dndSwitchOnChecked(state: Boolean) {
        _dndSwitchState.value = !_dndSwitchState.value
    }

    /**
     * DND 开始时间
     * */
    private val start: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 24)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val _dndStartState = MutableStateFlow(start.time)
    val dndStartState = _dndStartState.asStateFlow()

    fun setDNDStart(start: Date) {
        _dndStartState.value = start
    }

    /**
     * DND 结束时间
     * */
    private val end: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 6)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val _dndEndState = MutableStateFlow(end.time)
    val dndEndState = _dndEndState.asStateFlow()

    fun setDNDEnd(end: Date) {
        _dndEndState.value = end
    }

    /**
     * 值太小
     * */

    fun onUpscaleClicked() {
        Log.v("SEVM", "UP")
    }

    /**
     * 值太大
     * */
    fun onDownscaleClicked() {
        Log.v("SEVM", "DOWN")
    }
}