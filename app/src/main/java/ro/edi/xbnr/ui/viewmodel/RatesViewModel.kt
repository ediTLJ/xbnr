package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.data.remote.model.BnrCurrency
import ro.edi.xbnr.data.remote.model.BnrRates
import ro.edi.xbnr.util.Helper

class RatesViewModel(application: Application) : AndroidViewModel(application) {
    private var rates: LiveData<BnrRates>? = null

    fun getRates(): LiveData<BnrRates> {
        if (rates == null) {
            rates = DataManager.getInstance(getApplication()).getLatestRates()
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
