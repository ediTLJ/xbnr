package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.util.Helper

class RatesViewModel(application: Application) : AndroidViewModel(application) {
    private var currencies: LiveData<List<Currency>>? = null

    fun getCurrencies(): LiveData<List<Currency>> {
        if (currencies == null) {
            currencies = DataManager.getInstance(getApplication()).getRates()
        }
        return currencies as LiveData<List<Currency>>
    }

    fun getCurrency(position: Int): Currency? {
        return getCurrencies().value?.get(position)
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
