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
import timber.log.Timber.i as logi

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

    fun getRate(): Double {
        // FIXME multiplier

        val fromRate = getFrom()?.rate
        fromRate ?: return 0.0

        val toRate = getTo()?.rate
        toRate ?: return 0.0

        if (toRate == 0.0) {
            return 0.0
        }

        logi("rate: ${fromRate.div(toRate)}")

        return fromRate.div(toRate)
    }

    fun getDisplayRate(context: Context): String? {
        // e.g. €1 = lei 4.7599

        val fromMultiplier = getFrom()?.multiplier
        fromMultiplier ?: return null

        val fromCode = getFrom()?.code
        fromCode ?: return null

        val toCode = getTo()?.code
        toCode ?: return null

        val fromSymbol = context.getText(Helper.getCurrencySymbolRes(fromCode))
        val toSymbol = context.getText(Helper.getCurrencySymbolRes(toCode))

        val sb = StringBuilder(32)
        sb.append(fromSymbol)
        sb.append(fromMultiplier)
        sb.append(" = ")
        sb.append(toSymbol)
        sb.append(nf.format(getRate()))

        return sb.toString()
    }

//    fun getDisplayDate(rate: DateRate): String {
//        return LocalDate.parse(rate.date)
//            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
//    }

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