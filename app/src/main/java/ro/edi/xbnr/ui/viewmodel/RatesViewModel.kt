package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.util.Helper

class RatesViewModel(application: Application) : AndroidViewModel(application) {
    val currencies: LiveData<List<Currency>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getRates()
    }

    fun getCurrency(position: Int): Currency? {
        return currencies.value?.getOrNull(position)
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
