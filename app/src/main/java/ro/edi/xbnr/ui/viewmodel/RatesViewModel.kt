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
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.Rate
import ro.edi.xbnr.ui.util.Helper
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class RatesViewModel(application: Application) : AndroidViewModel(application) {
    val isFetching = DataManager.getInstance(getApplication()).isFetching as LiveData<Boolean>

    val previousRates: LiveData<List<Rate>> =
        DataManager.getInstance(getApplication()).getPreviousRates()

    val currencies: LiveData<List<Currency>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getRates()
    }

    private val nf = NumberFormat.getNumberInstance().apply {
        roundingMode = RoundingMode.HALF_UP
        minimumFractionDigits = 4
        maximumFractionDigits = 4
    }

    fun refresh() {
        DataManager.getInstance(getApplication()).fetchRates()
    }

    fun getCurrency(position: Int): Currency? {
        return currencies.value?.getOrNull(position)
    }

    fun getCurrencyId(position: Int): Int {
        getCurrency(position)?.let {
            return it.id
        }

        return 0
    }

    fun getCurrencyDisplayCode(position: Int): String? {
        return getCurrency(position)?.let {
            if (it.multiplier > 1) {
                (getApplication() as Application).resources.getQuantityString(
                    R.plurals.currency_multiplier,
                    it.multiplier,
                    it.multiplier,
                    it.code
                )
            } else it.code
        }
    }

    fun getCurrencyDisplayRate(position: Int): String? {
        return getCurrency(position)?.let {
            nf.format(it.rate)
        }
    }

    fun getCurrencyIconRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyIconRes(currency?.code)
    }

    fun getCurrencyNameRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyNameRes(currency?.code)
    }

    fun getCurrencyDisplayDate(position: Int): String? {
        return getCurrency(position)?.let {
            LocalDate.parse(it.date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }

    private fun getTrend(position: Int): Int {
        val currency: Currency = getCurrency(position) ?: return 0

        previousRates.value?.let {
            for (previous in it) {
                if (previous.currencyId == currency.id) {
                    return when {
                        currency.rate > previous.rate -> 1
                        currency.rate < previous.rate -> -1
                        else -> 0
                    }
                }
            }
        }

        return 0
    }

    fun getTrendColorRes(context: Context, position: Int): Int {
        val trend = getTrend(position)
        return when {
            trend > 0 -> R.color.textColorTrendUp
            trend < 0 -> R.color.textColorTrendDown
            else -> getColorRes(
                context,
                android.R.attr.textColorPrimary
            )
        }
    }

    fun getCurrencyTextColorRes(context: Context, position: Int, isPrimary: Boolean): Int {
        getCurrency(position)?.let {
            return if (isPrimary)
                if (it.isStarred) R.color.textColorStarredPrimary else getColorRes(
                    context,
                    android.R.attr.textColorPrimary
                )
            else
                if (it.isStarred) R.color.textColorStarredSecondary else getColorRes(
                    context,
                    android.R.attr.textColorSecondary
                )
        }

        return getColorRes(
            context,
            if (isPrimary) android.R.attr.textColorPrimary else android.R.attr.textColorSecondary
        )
    }

    fun setIsStarred(position: Int, isStarred: Boolean) {
        getCurrency(position)?.let {
            DataManager.getInstance(getApplication()).update(it, isStarred)
        }
    }
}