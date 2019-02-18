package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.xbnr.R
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyRate
import ro.edi.xbnr.util.Helper
import ro.edi.xbnr.util.getColorRes

class RatesViewModel(application: Application) : AndroidViewModel(application) {
    val currencies: LiveData<List<Currency>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getRates()
    }

    val previousRates: LiveData<List<CurrencyRate>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getPreviousRates()
    }

    fun getCurrency(position: Int): Currency? {
        return currencies.value?.getOrNull(position)
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

    fun getCurrencyIconRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyIconRes(currency?.code)
    }

    fun getCurrencyNameRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyNameRes(currency?.code)
    }

    fun getTrend(position: Int): Int {
        val currency: Currency = getCurrency(position) ?: return 0

        previousRates.value?.let {
            for (previous in it) {
                if (previous.currency_id == currency.id) {
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

    fun getTrendColorRes(position: Int): Int {
        val trend = getTrend(position)
        return when {
            trend > 0 -> R.color.orange_300
            trend < 0 -> R.color.green_300
            else -> R.color.white
        }
    }

    fun getIsStarredColorRes(context: Context, position: Int): Int {
        getCurrency(position)?.let {
            return if (it.isStarred) R.color.yellow_300 else getColorRes(
                context,
                android.R.attr.textColorPrimary
            )
        }

        return getColorRes(context, android.R.attr.textColorPrimary)
    }

    fun setIsStarred(position: Int, isStarred: Boolean) {
        getCurrency(position)?.let {
            DataManager.getInstance(getApplication()).update(it, isStarred)
        }
    }
}
