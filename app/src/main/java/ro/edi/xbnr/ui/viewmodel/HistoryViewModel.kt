/*
* Copyright 2019 Eduard Scarlat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.DateRate

const val PREFS_KEY_CHART_INTERVAL = "chart_interval"

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private var currencyId = -1
    var chartHighlight: DateRate? = null

    private val countLiveData = MutableLiveData<Int>()
    private lateinit var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener

    val rates: LiveData<List<DateRate>> = Transformations.switchMap(
        countLiveData
    ) { count ->
        DataManager.getInstance(getApplication()).getRates(currencyId, count)
    }

    constructor(application: Application, currencyId: Int) : this(application) {
        this.currencyId = currencyId

        prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (PREFS_KEY_CHART_INTERVAL == key) {
                countLiveData.value = when (sharedPrefs.getInt(key, 1)) {
                    1 -> 20
                    3 -> 64
                    12 -> 260
                    else -> 20
                }
            }
        }

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(application)
        countLiveData.value = when (sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)) {
            1 -> 20
            3 -> 64
            12 -> 260
            else -> 20
        }

        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    fun getDisplayRate(rate: DateRate): String {
        return String.format("%.4f", rate.rate)
    }

    fun getDisplayDate(rate: DateRate): String {
        return LocalDate.parse(rate.date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }
}