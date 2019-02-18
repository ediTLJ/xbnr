package ro.edi.xbnr.data

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import ro.edi.xbnr.data.db.AppDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyRate
import ro.edi.xbnr.util.Singleton
import ro.edi.xbnr.util.logd
import ro.edi.xbnr.util.loge
import ro.edi.xbnr.util.logi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

    fun update(currency: Currency, isStarred: Boolean) {
        executor.execute {
            val dbCurrency =
                DbCurrency(currency.id, currency.code, currency.multiplier, isStarred)
            db.currencyDao().update(dbCurrency)
        }
    }

    /**
     * Get latest available rates.
     *
     * This also triggers a call to get latest data from the server, if needed.
     */
    fun getRates(): LiveData<List<Currency>> {
        executor.execute {
            val latestDateString = db.rateDao().getLatestDate()

            // FIXME test if it works for all locales & timezones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val zoneIdRomania = ZoneId.of("Europe/Bucharest")
                val today = LocalDate.now(zoneIdRomania)
                    .also { logi(TAG, "today: ", it) }

                if (latestDateString.isNullOrEmpty()) {
                    logi(TAG, "no date in the db")
                    fetchRates(10)
                    // fetchRates(today.year)
                    // fetchRates(today.year - 1)
                    return@execute
                }

                val latestDate = LocalDate.parse(latestDateString)
                    .also { logi(TAG, "latest date: ", it) }

                val previousWorkday =
                    if (today.dayOfWeek == DayOfWeek.MONDAY)
                        today.minusDays(3)
                    else today.minusDays(1)

                // another option would be to use Period.between(latestDate, today)

                // if latestDate == today => all good, don't do anything
                if (latestDate.isBefore(today.minusWeeks(1))) {
                    // FIXME add service to fetch data weekly?
                    // so we should never reach this
                    fetchRates(today.year)
                    fetchRates(today.year - 1)
                } else if (latestDate.isBefore(previousWorkday)) {
                    fetchRates(10)
                } else if (latestDate == previousWorkday) {
                    val now = LocalTime.now(zoneIdRomania)
                        .also { logi(TAG, "now: ", it) }
                    val hour1pm = LocalTime.of(13, 0)

                    if (now.isAfter(hour1pm)) {
                        fetchRates(1)
                    } else { // before 1pm
                        // no rates published yet, no need to do anything
                        logi(TAG, "before 1pm => nothing to do")
                    }
                } else { // today
                    // all good, don't do anything
                    logi(TAG, "today => nothing to do")
                }
            } else {
                // FIXME support for Android pre-Oreo
                // val date = Date();
            }
        }

        return db.rateDao().getRates()
    }

    /**
     * Get previous available rates.
     */
    fun getPreviousRates(): LiveData<List<CurrencyRate>> {
        return db.rateDao().getPreviousRates()
    }

    /**
     * Get all rates for the specified interval.
     *
     * **Don't call this on the main UI thread!**
     *
     * @param interval
     *     1 => 1 day (latest rates)
     *     10 => 10 days (last 10 days)
     *     2005 or more => year 2005 or more
     *     other => 1 day (latest rates)
     */
    private fun fetchRates(interval: Int) {
        val call =
            when {
                interval == 1 -> BnrService.instance.latestRates
                interval == 10 -> BnrService.instance.last10Rates
                interval >= 2005 -> BnrService.instance.rates(interval)
                else -> BnrService.instance.latestRates
            }

        logi(TAG, "fetching ", interval)

        val response = runCatching { call.execute() }.getOrNull()
        response ?: return

        if (response.isSuccessful) {
            val days = response.body() ?: return
            days.ratesList ?: return

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
