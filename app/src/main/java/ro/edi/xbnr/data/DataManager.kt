/*
* Copyright 2019 Eduard Scarlat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.edi.xbnr.data

import android.app.Application
import androidx.lifecycle.LiveData
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import ro.edi.util.AppExecutors
import ro.edi.util.Singleton
import ro.edi.xbnr.data.db.AppDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyRate
import timber.log.Timber.d as logd
import timber.log.Timber.e as loge
import timber.log.Timber.i as logi

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

    init {
        // ...
    }

    companion object : Singleton<DataManager, Application>(::DataManager)

    fun update(currency: Currency, isStarred: Boolean) {
        AppExecutors.diskIO().execute {
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
        AppExecutors.networkIO().execute {
            val latestDateString = db.rateDao().getLatestDate()

            // FIXME test if it works for all locales & timezones
            val zoneIdRomania = ZoneId.of("Europe/Bucharest")
            val today = LocalDate.now(zoneIdRomania)
                .also { logi("today: %s", it) }

            if (latestDateString.isNullOrEmpty()) {
                logi("no date in the db")
                fetchRates(10)
                // fetchRates(today.year)
                // fetchRates(today.year - 1)
                return@execute
            }

            val latestDate = LocalDate.parse(latestDateString)
                .also { logi("latest date: %s", it) }

            val previousWorkday =
                if (today.dayOfWeek == DayOfWeek.MONDAY)
                    today.minusDays(3)
                else today.minusDays(1)

            // another option would be to use Period.between(latestDate, today)

            // if latestDate == today => all good, don't do anything
            if (latestDate.isBefore(today.minusWeeks(1))) {
                // TODO add service to fetch data weekly? so we should never reach this
                fetchRates(today.year)
                fetchRates(today.year - 1)
            } else if (latestDate.isBefore(previousWorkday)) {
                fetchRates(10)
            } else if (latestDate == previousWorkday) {
                val now = LocalTime.now(zoneIdRomania)
                    .also { logi("now: %s", it) }
                val hour1pm = LocalTime.of(13, 0)

                if (now.isAfter(hour1pm)) {
                    fetchRates(1)
                } else { // before 1pm
                    // no rates published yet, no need to do anything
                    logi("before 1pm => nothing to do")
                }
            } else { // today
                // all good, don't do anything
                logi("today => nothing to do")
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

        logi("fetching %d", interval)

        val response = runCatching { call.execute() }.getOrElse {
            loge(it, "error fetching or parsing rates")
            return
        }

        if (response.isSuccessful) {
            val days = response.body() ?: return
            days.ratesList ?: return

            db.runInTransaction {
                for (rates in days.ratesList) {
                    rates.date ?: return@runInTransaction
                    rates.currencies ?: return@runInTransaction

                    logd("date: %s", rates.date)

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
            // ignore errors, for now
            loge("error fetching rates [%d]: %s", response.code(), response.errorBody())
        }
    }
}