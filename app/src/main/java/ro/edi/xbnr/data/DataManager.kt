package ro.edi.xbnr.data

import android.app.Application
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.db.RatesDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.util.Log
import ro.edi.xbnr.util.Singleton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * This class manages the underlying data.
 *
 * Data sources can be local (e.g. db) or remote (e.g. REST APIs).
 *
 * All methods should return model objects only.
 *
 * **Warning:**
 *
 * **This shouldn't expose any of the underlying data to the application layers above.**
 */
class DataManager private constructor(application: Application) {
    private val db: RatesDatabase by lazy { RatesDatabase.getInstance(application) }
    private val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    init {
        // ...
    }

    companion object : Singleton<DataManager, Application>(::DataManager) {
        private const val TAG = "RATES.MANAGER"
    }

    /**
     * Get latest available rates.
     *
     * This also triggers a call to get latest data from the server, if needed.
     */
    fun getRates(): LiveData<List<Currency>> {
        fetchLatestRates()

        return db.rateDao().getRates()
    }

    private fun fetchLatestRates() {
        executor.execute {
            // FIXME add date conditions, for example:
            // #1 after 1pm and rateDao().getRates() returns a previous workday => fetchLatestRates()
            // OR
            // #2  rateDao().getRates() returns an older workday => fetchRates(int count), where days == days count

            val response =
                kotlin.runCatching { BnrService.instance.latestRates.execute() }.getOrNull()
            response ?: return@execute

            if (response.isSuccessful) {
                val rates = response.body() ?: return@execute

                Log.i(TAG, "rates: ", rates)

                rates.date ?: return@execute
                rates.currencies ?: return@execute

                db.runInTransaction {
                    for (currency in rates.currencies) {
                        Log.i(TAG, "currency: ", currency)
                        val id: Int = currency.code.hashCode()

                        val dbCurrency = DbCurrency(id, currency.code, currency.multiplier, false)
                        db.currencyDao().insert(dbCurrency)

                        val dbRate = DbRate(0, id, rates.date, currency.rate.toDouble())
                        db.rateDao().insert(dbRate)
                    }
                }
            } else {
                // FIXME handle errors
            }
        }
    }
}
