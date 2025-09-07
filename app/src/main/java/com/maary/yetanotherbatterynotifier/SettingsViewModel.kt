package com.maary.yetanotherbatterynotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maary.yetanotherbatterynotifier.service.ForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val application: Context,
    private val preferenceRepository: PreferenceRepository
): ViewModel() {

    companion object {
        val FREQUENCY_OPTIONS = mutableListOf("1s", "2s", "5s", "1min")
        val FREQUENCY_LONG = mutableListOf(1000L, 2000L, 5000L, 60000L)
    }

    /**
     * 是否启用前台服务
     * */
    private val _foregroundSwitchState = MutableStateFlow(false)
    val foregroundSwitchState: StateFlow<Boolean> = _foregroundSwitchState.asStateFlow()

    private fun restartForegroundService() {
        viewModelScope.launch {
            val intent = Intent(application, ForegroundService::class.java)
            application.stopService(intent)
            delay(1500)
            application.startForegroundService(intent)
        }

    }
    fun foregroundSwitchOnChecked(state: Boolean) {
        val intent = Intent(application, ForegroundService::class.java)
        if (!_foregroundSwitchState.value){
            createNotificationChannel(
                NotificationManager.IMPORTANCE_MIN,
                application.getString(R.string.default_channel),
                application.getString(R.string.default_channel_description)
            )
            createNotificationChannel(
                NotificationManager.IMPORTANCE_DEFAULT,
                application.getString(R.string.channel_notify),
                application.getString(R.string.channel_notify_description)
            )
            viewModelScope.launch { preferenceRepository.setServiceState(true) }

            application.startForegroundService(intent)
        }else {
            viewModelScope.launch { preferenceRepository.setServiceState(false) }

            application.stopService(intent)
        }
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
        restartForegroundService()
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
        restartForegroundService()
    }

    /**
     * 是否启用常态显示
     * */
    private val _alwaysOnSwitchState = MutableStateFlow(false)
    val alwaysOnSwitchState: StateFlow<Boolean> = _alwaysOnSwitchState.asStateFlow()

    fun alwaysOnSwitchOnChecked(state: Boolean) {
        viewModelScope.launch { preferenceRepository.setAlwaysShow(state) }
        restartForegroundService()
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
        restartForegroundService()
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
        restartForegroundService()
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
        if (_dndSwitchState.value) {
            restartForegroundService()
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
        if (_dndSwitchState.value) {
            restartForegroundService()
        }
    }

    private val _fuckOEMEnabled = MutableStateFlow(false)

    private val _oemTitle = MutableStateFlow(R.string.fuck_oem_regular)
    private val _oemDescription = MutableStateFlow(R.string.fuck_oem_regular_description)
    val oemTitle = _oemTitle.asStateFlow()
    val oemDescription = _oemDescription.asStateFlow()

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

    /**
     * 值太小
     * */
    fun onUpscaleClicked() {
        viewModelScope.launch {
            preferenceRepository.setRatio(1)
        }
        restartForegroundService()
    }

    /**
     * 值太大
     * */
    fun onDownscaleClicked() {
        viewModelScope.launch {
            preferenceRepository.setRatio(1000)
        }
        restartForegroundService()
    }

    fun onNegativeClicked() {
        viewModelScope.launch {
            preferenceRepository.flipNegativeIsCharging()
        }
        restartForegroundService()
    }

    init {
        viewModelScope.launch { //
            val desiredServiceState = preferenceRepository.getServiceEnabled().first() // 从 DataStore 获取期望状态
            val actualServiceRunning = ForegroundService.isForegroundServiceRunning.first() // 获取服务的实际运行状态

            // 如果期望状态与实际运行状态不一致，则进行协调
            if (desiredServiceState != actualServiceRunning) { //
                val intent = Intent(application, ForegroundService::class.java) //
                if (desiredServiceState) { // 期望开启但实际未运行，则启动服务
                    application.startForegroundService(intent)
                } else { // 期望关闭但实际正在运行，则停止服务
                    application.stopService(intent)
                }
            }
        }
        preferenceRepository.getLevel1().onEach {
            _notifyLevel1State.value = it.toFloat()
        }.launchIn(viewModelScope)
        preferenceRepository.getLevel2().onEach{
            _notifyLevel2State.value = it.toFloat()
        }.launchIn(viewModelScope)
        preferenceRepository.getAlwaysShow().onEach {
            _alwaysOnSwitchState.value = it
        }.launchIn(viewModelScope)
        preferenceRepository.getFrequency().onEach {
            val index = FREQUENCY_LONG.indexOf(it)
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
        ForegroundService.isForegroundServiceRunning.onEach {
            _foregroundSwitchState.value = it
        }.launchIn(viewModelScope)
    }

    private fun createNotificationChannel(importance:Int, name:String, descriptionText: String) {
        //val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(name)!=null) return
        notificationManager.createNotificationChannel(channel)
    }
}