package com.maary.yetanotherbatterynotifier

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_fragment, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference, rootKey)

            val sharedPreference =
                context?.getSharedPreferences(getString(R.string.name_shared_pref), Context.MODE_PRIVATE)

            // 获取对应的 SwitchPreferenceCompat 和 ListPreference 对象
            val showCurrentSwitch: SwitchPreference = findPreference(getString(R.string.boolean_always_show_speed))!!
            val updateRateList: ListPreference = findPreference(getString(R.string.shared_pref_frequency))!!
            val currentRatioList: ListPreference = findPreference(getString(R.string.shared_pref_ratio))!!

            showCurrentSwitch.isChecked = sharedPreference?.getBoolean(getString(R.string.boolean_always_show_speed), false)!!

            showCurrentSwitch.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener{ _, newValue ->
                    sharedPreference.edit().putBoolean(getString(R.string.boolean_always_show_speed), newValue as Boolean).apply()
                    true
                }

            val updateFrequencyValues = resources.getStringArray(R.array.update_frequency_values)
            val updateFrequency = sharedPreference.getLong(getString(R.string.shared_pref_frequency), 60000L)

            val defaultIndex = updateFrequencyValues.indexOf(updateFrequency.toString())
            Log.v("YABN", defaultIndex.toString())
            Log.v("YABN", updateRateList.value)
            updateRateList.setValueIndex(defaultIndex)

            updateRateList.setOnPreferenceChangeListener { _, newValue ->

                sharedPreference.edit().putLong(getString(R.string.shared_pref_frequency), newValue.toString().toLong()).apply()
                true
            }

            val currentRatioValues = resources.getStringArray(R.array.current_ratio_values)
            val currentRatio = sharedPreference.getInt(getString(R.string.shared_pref_ratio), 1000)

            val defaultRatioIndex = currentRatioValues.indexOf(currentRatio.toString())
            currentRatioList.setValueIndex(defaultRatioIndex)

            currentRatioList.setOnPreferenceChangeListener{ _, newValue ->
                sharedPreference.edit().putInt(getString(R.string.shared_pref_ratio), newValue.toString().toInt()).apply()
                true
            }


            // 在这里可以对 SwitchPreferenceCompat 和 ListPreference 进行相应的操作
            // 例如添加监听器，在值发生变化时做出相应的响应
        }
    }

}