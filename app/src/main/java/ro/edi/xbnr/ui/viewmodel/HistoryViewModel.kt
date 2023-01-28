/*
* Copyright 2019-2023 Eduard Scarlat
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
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.data.Entry
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.DayRate
import ro.edi.xbnr.model.MonthRate
import ro.edi.xbnr.model.YearRate
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

class HistoryViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var currencyId: Int
        get() = savedStateHandle[KEY_CURRENCY_ID] ?: 0
        set(id) {
            savedStateHandle[KEY_CURRENCY_ID] = id
        }

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    private val prefsListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (PREFS_KEY_CHART_INTERVAL == key) {
                monthsCountLiveData.value = sharedPrefs.getInt(key, 1)
            }
        }

    private val monthsCountLiveData = MutableLiveData<Int>().apply {
        val monthsCount = sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)
        value = if (monthsCount == 3) {
            // 3 months option is deprecated... it was replaced by 6 months
            6
        } else {
            monthsCount
        }
    }

    val dayRates: LiveData<List<DayRate>> by lazy(LazyThreadSafetyMode.NONE) {
        monthsCountLiveData.switchMap { monthsCount ->
            if (monthsCount == 60 || monthsCount == -1) {
                // return empty LiveData if 5Y or MAX data requested
                return@switchMap MutableLiveData<List<DayRate>>()
            }
            DataManager.getInstance(application).getDayRates(currencyId, monthsCount)
        }
    }

    val monthRates: LiveData<List<MonthRate>> by lazy(LazyThreadSafetyMode.NONE) {
        monthsCountLiveData.switchMap { monthsCount ->
            if (monthsCount != 60) {
                // return empty LiveData if not 5Y data requested
                return@switchMap MutableLiveData<List<MonthRate>>()
            }
            DataManager.getInstance(application).getMonthRates(currencyId, monthsCount)
        }
    }

    val yearRates: LiveData<List<YearRate>> by lazy(LazyThreadSafetyMode.NONE) {
        monthsCountLiveData.switchMap { monthsCount ->
            if (monthsCount != -1) {
                // return empty LiveData if not MAX data requested
                return@switchMap MutableLiveData<List<YearRate>>()
            }

            DataManager.getInstance(application).getYearRates(currencyId)
        }
    }

    private val nf = NumberFormat.getNumberInstance().apply {
        roundingMode = RoundingMode.HALF_UP
        minimumFractionDigits = 4
        maximumFractionDigits = 4
    }
    private val nfPercent = NumberFormat.getNumberInstance().apply {
        roundingMode = RoundingMode.HALF_UP
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    var highlightedEntry: Entry? = null

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    fun getDisplayRate(rate: Double): String {
        return nf.format(rate)
    }

    fun getDisplayTrend(rate: DayRate): String {
        val trend = StringBuilder(16)

        dayRates.value?.let {
            val idxRate = it.indexOf(rate)
            if (idxRate <= 0) {
                return@let
            }

            val diff = rate.rate - it[idxRate - 1].rate

            if (diff > 0) {
                trend.append('+')
            }
            trend.append(nf.format(diff))
            trend.append(' ')
            trend.append('(')
            trend.append(nfPercent.format(abs(diff).times(100).div(rate.rate)))
            trend.append('%')
            trend.append(')')
        }

        return trend.toString()
    }

    fun getDisplayDate(date: String): String {
        return LocalDate.parse(date)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

    fun getDisplayMonth(month: String): String {
        val yearMonth = YearMonth.parse(month)
        return yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).plus(' ')
            .plus(yearMonth.year)
    }

    companion object {
        private const val KEY_CURRENCY_ID = "currency-id"

        const val PREFS_KEY_CHART_INTERVAL = "chart_interval"

        val FACTORY = viewModelFactory {
            // the return type of the lambda automatically sets what class this lambda handles
            initializer {
                // get the Application object from extras provided to the lambda
                val application = checkNotNull(this[APPLICATION_KEY])

                val savedStateHandle = createSavedStateHandle()

                HistoryViewModel(
                    application = application,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}