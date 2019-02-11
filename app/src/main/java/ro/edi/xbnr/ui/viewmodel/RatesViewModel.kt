package ro.edi.xbnr.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ro.edi.xbnr.data.RatesManager
import ro.edi.xbnr.data.model.BnrCurrency
import ro.edi.xbnr.data.model.BnrRates
import ro.edi.xbnr.util.Helper

class RatesViewModel internal constructor() : ViewModel() {
    private var rates: LiveData<BnrRates>? = null

    fun getRates(): LiveData<BnrRates> {
        if (rates == null) {
            rates = RatesManager.getLatestRates()
        }
        return rates as LiveData<BnrRates>
    }

    fun getCurrency(position: Int): BnrCurrency? {
        getRates().value?.currencies?.let {
            return it[position]
        }

        return null
    }

    fun getCurrencyIconRes(position: Int): Int {
        val currency: BnrCurrency? = getCurrency(position)

        return Helper.getCurrencyIconRes(currency?.code)
    }

    fun getCurrencyNameRes(position: Int): Int {
        val currency: BnrCurrency? = getCurrency(position)

        return Helper.getCurrencyNameRes(currency?.code)
    }
}
