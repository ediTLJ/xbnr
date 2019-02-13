package ro.edi.xbnr.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.edi.xbnr.data.db.RatesDatabase
import ro.edi.xbnr.data.db.entity.Rates
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.data.remote.model.BnrRates
import ro.edi.xbnr.util.SingletonHolder

class DataManager private constructor(application: Application) {
    // private const val TAG = "RATES.MANAGER"
    // private val db: RatesDatabase = RatesDatabase.getInstance(application)

    init {
        // ...
    }

    companion object : SingletonHolder<DataManager, Application>(::DataManager)

//    fun getRates(): LiveData<List<Rates>> {
//        getLatestRates()
//
//        return db.currencyDao().getRates()
//    }

    fun getLatestRates(): LiveData<BnrRates> {
        val data = MutableLiveData<BnrRates>()

        BnrService.instance.latestRates.enqueue(object : Callback<BnrRates> {
            override fun onResponse(call: Call<BnrRates>, response: Response<BnrRates>) {
                val rates = response.body() ?: return

                rates.date ?: return
                rates.currencies ?: return

                // FIXME check rates date
                data.value = rates
            }

            override fun onFailure(call: Call<BnrRates>, t: Throwable) {
                // FIXME show error
                data.value = BnrRates(null, null)
            }
        })
        return data
    }
}
