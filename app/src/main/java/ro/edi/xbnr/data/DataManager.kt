package ro.edi.xbnr.data

import android.app.Application
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.db.AppDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.util.Singleton
import ro.edi.xbnr.util.logd
import ro.edi.xbnr.util.loge
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
    private val db: AppDatabase by lazy { AppDatabase.getInstance(application) }
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
        // FIXME add date conditions, for example:
        // #1 after 1pm and rateDao().getRates() returns a previous workday => fetchLatestRates()
        // OR
        // #2  rateDao().getRates() returns an older workday => fetchRates(int count), where days == days count

        fetchRates(10)

        // FIXME is the db query done async?
        return db.rateDao().getRates()
    }

    /**
     * Get all rates for the specified interval.
     *
     * @param interval
     *     1 => 1 day (latest rates)
     *     10 => 10 days (last 10 days)
     *     2005 or more => year 2005 or more
     *     other => 1 day (latest rates)
     */
    private fun fetchRates(interval: Int) {
        executor.execute {

            val call =
                when (interval) {
                    1 -> BnrService.instance.latestRates
                    10 -> BnrService.instance.last10Rates
                    in 2005..Int.MAX_VALUE -> BnrService.instance.rates(interval)
                    else -> BnrService.instance.latestRates
                }

            val response = runCatching { call.execute() }.getOrNull()
            response ?: return@execute

            if (response.isSuccessful) {
                val days = response.body() ?: return@execute
                days.ratesList ?: return@execute

                db.runInTransaction {
                    for (rates in days.ratesList) {
                        rates.date ?: return@runInTransaction
                        rates.currencies ?: return@runInTransaction

                        logd(TAG, "date: ", rates.date)

                        for (currency in rates.currencies) {
                            // logd(TAG, "currency: ", currency)
                            val id: Int = currency.code.hashCode()

                            // the tikXML library ignores the default value set in the model
                            val multiplier = if (currency.multiplier > 0) currency.multiplier else 1

                            // some rates are missing (having the "-" value, for example)
                            currency.rate.toDoubleOrNull()?.let {
                                val dbCurrency = DbCurrency(id, currency.code, multiplier, false)
                                db.currencyDao().insert(dbCurrency)

                                val dbRate = DbRate(0, id, rates.date, it)
                                db.rateDao().insert(dbRate)
                            } // else skip this currency rate
                        }
                    }
                }
            } else {
                loge(TAG, response.errorBody())
                // FIXME handle errors
            }
        }
    }
}
