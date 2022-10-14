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
import android.content.Context
import androidx.lifecycle.*
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyMinimal
import ro.edi.xbnr.ui.util.Helper
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import timber.log.Timber.Forest.i as logi

class ConverterViewModel(application: Application) : AndroidViewModel(application) {
    private val fromCurrencyId = MutableLiveData<Int>()
    private val toCurrencyId = MutableLiveData<Int>()
    private var date = MutableLiveData<String>()

    val currencies: LiveData<List<CurrencyMinimal>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(application).getCurrencies()
    }

    val fromCurrency: LiveData<Currency> by lazy(LazyThreadSafetyMode.NONE) {
        Transformations.switchMap(RateLiveData(fromCurrencyId, date)) {
            logi("from switchMap")
            if (it.currencyId == null || it.currencyId == 0) {
                return@switchMap MutableLiveData(Currency(0, "RON", 1, false, "", 1.0))
            }
            DataManager.getInstance(application).getCurrency(it.currencyId, it.date)
        }
    }

    val toCurrency: LiveData<Currency> by lazy(LazyThreadSafetyMode.NONE) {
        Transformations.switchMap(RateLiveData(toCurrencyId, date)) {
            logi("to switchMap")
            if (it.currencyId == null || it.currencyId == 0) {
                return@switchMap MutableLiveData(Currency(0, "RON", 1, false, "", 1.0))
            }
            DataManager.getInstance(application).getCurrency(it.currencyId, it.date)
        }
    }

    private val nf = NumberFormat.getNumberInstance()

    constructor(
        application: Application,
        fromCurrencyId: Int,
        toCurrencyId: Int,
        date: String?
    ) : this(application) {
        this.fromCurrencyId.value = fromCurrencyId
        this.toCurrencyId.value = toCurrencyId
        this.date.value = date

        // logi("from id: %s", fromCurrencyId)
        // logi("to id: %s", toCurrencyId)
        // logi("date: %s", date)

        nf.roundingMode = RoundingMode.HALF_UP
        nf.minimumFractionDigits = 4
        nf.maximumFractionDigits = 4
    }

    fun updateSource(fromCurrencyId: Int, toCurrencyId: Int, date: String?) {
        this.fromCurrencyId.value = fromCurrencyId
        this.toCurrencyId.value = toCurrencyId
        this.date.value = date
    }

    fun getFrom(): Currency? {
        return fromCurrency.value
    }

    fun getTo(): Currency? {
        return toCurrency.value
    }

    fun getFromIconRes(): Int {
        return Helper.getCurrencyIconRes(getFrom()?.code)
    }

    fun getToIconRes(): Int {
        return Helper.getCurrencyIconRes(getTo()?.code)
    }

    /**
     * Get the rate to be used for currency conversion, by also taking multipliers into account.
     */
    fun getRate(): Double {
        val from = getFrom()
        from ?: return 0.0

        val to = getTo()
        to ?: return 0.0

        if (to.rate == 0.0) {
            return 0.0
        }

        val rate = from.rate.times(to.multiplier).div(to.rate.times(from.multiplier))

        logi("rate: $rate")
        return rate
    }

    fun getDisplayRate(context: Context): String? {
        // e.g. €1 = lei4.7599

        val from = getFrom()
        from ?: return null

        val to = getTo()
        to ?: return null

        val fromSymbol = context.getText(Helper.getCurrencySymbolRes(from.code))
        val toSymbol = context.getText(Helper.getCurrencySymbolRes(to.code))

        val sb = StringBuilder(32)
        sb.append(fromSymbol)
        sb.append(from.multiplier)
        sb.append(" = ")
        sb.append(toSymbol)
        sb.append(nf.format(from.rate.div(to.rate)))

        return sb.toString()
    }

    fun getDisplayDate(): String {
        val from = getFrom()
        from ?: return ""

        val to = getTo()
        to ?: return ""

        // RON date is set to "" in the constructor
        val date = from.date.ifEmpty { to.date }

        return LocalDate.parse(date)
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

    data class RateSource<Int, String>(
        val currencyId: Int,
        val date: String
    )

    class RateLiveData<Int, String>(currencyId: LiveData<Int>, date: LiveData<String>) :
        MediatorLiveData<RateSource<Int?, String?>>() {
        init {
            addSource(currencyId) { value = RateSource(it, date.value) }
            addSource(date) { value = RateSource(currencyId.value, it) }
        }
    }
}