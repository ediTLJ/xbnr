package ro.edi.xbnr.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ro.edi.xbnr.data.model.Currency
import ro.edi.xbnr.data.CurrencyRepository
import ro.edi.xbnr.data.model.Rates
import ro.edi.xbnr.util.Helper

class RatesViewModel internal constructor() : ViewModel() {
    private var rates: LiveData<Rates>? = null

    fun getRates(): LiveData<Rates> {
        if (rates == null) {
            rates = CurrencyRepository.getLatestRates()
        }
        return rates as LiveData<Rates>
    }

    fun getCurrency(position: Int): Currency? {
        getRates().value?.currencies?.let {
            return it[position]
        }

        return null
    }

    fun getCurrencyIconRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyIconRes(currency?.code)
    }

    fun getCurrencyNameRes(position: Int): Int {
        val currency: Currency? = getCurrency(position)

        return Helper.getCurrencyNameRes(currency?.code)
    }
}
