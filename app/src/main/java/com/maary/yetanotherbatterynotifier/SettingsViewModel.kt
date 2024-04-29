package com.maary.yetanotherbatterynotifier

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferenceRepository: PreferenceRepository): ViewModel() {

    companion object {
        val FREQUENCY_OPTIONS = mutableListOf("1s", "2s", "5s", "1min")
        val FREQUENCY_LONG = mutableListOf(1000L, 2000L, 5000L, 60000L)
    }

    init {
            preferenceRepository.getLevel1().onEach {
                _notifyLevel1State.value = it.toFloat()
            }.launchIn(viewModelScope)
            preferenceRepository.getLevel2().onEach{
                Log.v("SEVM", it.toString())
                _notifyLevel2State.value = it.toFloat()
            }.launchIn(viewModelScope)
            preferenceRepository.getAlwaysShow().onEach {
                _alwaysOnSwitchState.value = it
            }.launchIn(viewModelScope)
            preferenceRepository.getFrequency().onEach {
                val index = FREQUENCY_LONG.indexOf(it)
                Log.v("SEVM", "FRE $index")

                _frequencyIndexState.value =
                    if (index != -1) index
                    else FREQUENCY_LONG.indexOf(5000L)
            }.launchIn(viewModelScope)
            preferenceRepository.getDndSet().onEach {
                _dndSwitchState.value = it
            }.launchIn(viewModelScope)
            preferenceRepository.getDndStartTime().onEach {
                _dndStartState.value= it ?: _dndStartState.value
            }.launchIn(viewModelScope)
            preferenceRepository.getDndEndTime().onEach {
                _dndEndState.value= it ?: _dndEndState.value
            }.launchIn(viewModelScope)
            preferenceRepository.getFuckOEM().onEach {
                _fuckOEMEnabled.value = it
                if (it) {
                    _oemTitle.value = R.string.fuck_oem_hidden
                    _oemDescription.value = R.string.fuck_oem_hidden_description
                } else {
                    _oemTitle.value = R.string.fuck_oem_regular
                    _oemDescription.value = R.string.fuck_oem_regular_description
                }
            }.launchIn(viewModelScope)
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
     * 电量提醒档位 1
     * */
    private val _notifyLevel1State = MutableStateFlow(0f)
    val notifyLevel1State = _notifyLevel1State.asStateFlow()

    fun onLevel1Changed(level: Float) {
        viewModelScope.launch {
            preferenceRepository.setLevel1(level.toInt())
        }
    }

    fun onLevel1Finished() {
        Log.v("SEVM", "LEVEL 1 FINISHED")
    }

    /**
     * 电量提醒档位 2
     * */
    private val _notifyLevel2State = MutableStateFlow(0f)
    val notifyLevel2State = _notifyLevel2State.asStateFlow()

    fun onLevel2Changed(level: Float) {
        viewModelScope.launch {
            preferenceRepository.setLevel2(level.toInt())
        }
    }

    fun onLevel2Finished() {
        Log.v("SEVM", "LEVEL 1 FINISHED")
    }

    /**
     * 是否启用常态显示
     * */
    private val _alwaysOnSwitchState = MutableStateFlow(false)
    val alwaysOnSwitchState: StateFlow<Boolean> = _alwaysOnSwitchState.asStateFlow()

    fun alwaysOnSwitchOnChecked(state: Boolean) {
        viewModelScope.launch { preferenceRepository.setAlwaysShow(state) }
    }


    /**
     * 更新频率
     * */
    private val _frequencyIndexState = MutableStateFlow(0)
    val frequencyIndexState = _frequencyIndexState.asStateFlow()

    fun frequencyItemClicked(index: Int) {
        viewModelScope.launch {
            preferenceRepository.setFrequency(FREQUENCY_LONG[index])
        }
    }

    /**
     * 是否启用 DND
     * */
    private val _dndSwitchState = MutableStateFlow(false)
    val dndSwitchState: StateFlow<Boolean> = _dndSwitchState.asStateFlow()

    fun dndSwitchOnChecked(state: Boolean) {
        viewModelScope.launch {
            preferenceRepository.setDndSet(state)
        }
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
        viewModelScope.launch {
            preferenceRepository.setDndStartTime(start)
        }
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
        viewModelScope.launch {
            preferenceRepository.setDndEndTime(end)
        }
    }

    /**
     * 值太小
     * */
    private val _fuckOEMEnabled = MutableStateFlow(false)
    val fuckOEMEnabled = _fuckOEMEnabled.asStateFlow()

    private val _oemTitle = MutableStateFlow(R.string.fuck_oem_regular)
    private val _oemDescription = MutableStateFlow(R.string.fuck_oem_regular_description)
    val oemTitle = _oemTitle.asStateFlow()
    val oemDescription = _oemDescription.asStateFlow()

    fun getOEMLabel(): Int {
        return if (_fuckOEMEnabled.value) {
            R.string.fuck_oem_hidden
        } else {
            R.string.fuck_oem_regular
        }
    }

    fun getOEMDescription(): Int {
        return if (_fuckOEMEnabled.value) {
            R.string.fuck_oem_hidden_description
        } else {
            R.string.fuck_oem_regular_description
        }
    }

    fun enableOEMLabel() {
        viewModelScope.launch {
            preferenceRepository.setFuckOEM(true)
            _oemTitle.value = R.string.fuck_oem_hidden
            _oemDescription.value = R.string.fuck_oem_hidden_description
        }
    }

    fun restoreOEMLabel() {
        viewModelScope.launch {
            preferenceRepository.setFuckOEM(false)
            _oemTitle.value = R.string.fuck_oem_regular
            _oemDescription.value = R.string.fuck_oem_regular_description
        }
    }

    fun onUpscaleClicked() {
        Log.v("SEVM", "UP")
        viewModelScope.launch {
            preferenceRepository.setRatio(1000)
        }
    }

    /**
     * 值太大
     * */
    fun onDownscaleClicked() {
        Log.v("SEVM", "DOWN")
        viewModelScope.launch {
            preferenceRepository.setRatio(1)
        }
    }
}