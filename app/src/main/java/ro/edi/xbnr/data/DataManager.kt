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
import androidx.lifecycle.MutableLiveData
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import ro.edi.util.AppExecutors
import ro.edi.util.Singleton
import ro.edi.xbnr.data.db.AppDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyMinimal
import ro.edi.xbnr.model.Rate
import ro.edi.xbnr.model.DayRate
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

    val isFetching = MutableLiveData<Boolean>()

    init {
        isFetching.value = true
    }

    companion object : Singleton<DataManager, Application>(::DataManager)

    /**
     * Get latest available rates.
     *
     * This also triggers a call to get latest data from the server, if needed.
     */
    fun getRates(): LiveData<List<Currency>> {
        AppExecutors.networkIO().execute {
            isFetching.postValue(true)

            val latestDateString = db.rateDao().getLatestDate()

            val zoneIdRomania = ZoneId.of("Europe/Bucharest")
            val today = LocalDate.now(zoneIdRomania)
                .also { logi("today: %s", it) }

            if (latestDateString.isNullOrEmpty()) {
                logi("no date in the db")
                fetchRates(today.year)
                fetchRates(today.year - 1)
                return@execute
            }

            val latestDate = LocalDate.parse(latestDateString)
                .also { logi("latest date: %s", it) }

            val previousWorkday =
                if (today.dayOfWeek == DayOfWeek.MONDAY) {
                    today.minusDays(3)
                } else {
                    today.minusDays(1)
                }

            // another option would be to use Period.between(latestDate, today)

            // FIXME what if latest date is older than 1-2 years ago? :)
            // if latestDate == today => all good, don't do anything
            if (latestDate.isBefore(today.minusWeeks(1))) {
                // add service to fetch data weekly? so we should never reach this
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
                    isFetching.postValue(false)
                }
            } else { // today
                // all good, don't do anything
                logi("today => nothing to do")
                isFetching.postValue(false)
            }
        }

        return db.rateDao().getRates()
    }

    /**
     * Get previous available rates.
     */
    fun getPreviousRates(): LiveData<List<Rate>> {
        return db.rateDao().getPreviousRates()
    }

    fun update(currency: Currency, isStarred: Boolean) {
        AppExecutors.diskIO().execute {
            val dbCurrency =
                DbCurrency(currency.id, currency.code, currency.multiplier, isStarred)
            db.currencyDao().update(dbCurrency)
        }
    }

    fun getCurrencies(): LiveData<List<CurrencyMinimal>> {
        return db.currencyDao().getCurrencies()
    }

    fun getCurrency(currencyId: Int): LiveData<Currency> {
        return db.rateDao().getCurrency(currencyId)
    }

    fun getCurrency(currencyId: Int, date: String?): LiveData<Currency> {
        if (date == null) {
            return db.rateDao().getCurrency(currencyId)
        }

        return db.rateDao().getCurrency(currencyId, date)
    }

    fun getRates(currencyId: Int, monthsCount: Int): LiveData<List<DayRate>> {
        val zoneIdRomania = ZoneId.of("Europe/Bucharest")
        val today = LocalDate.now(zoneIdRomania)

        val since = when (monthsCount) {
            0 -> {
                // FIXME fetch data if not stored in the db
                AppExecutors.networkIO().execute {
                    // isFetching.postValue(true)
                    // FIXME replace today.year - 1 with oldest year in the db & make sure it's >= 2005
                    // FIXME don't do anything if oldest year in the db is 2005+
                    //for (year in 2005 until today.year - 1 - 1) {
                    //    fetchRates(year)
                    //}
                }
                return db.rateDao().getRates(currencyId)
            }
            60 -> {
                // FIXME fetch data if not stored in the db
                AppExecutors.networkIO().execute {
                    // isFetching.postValue(true)
                    // FIXME replace today.year - 1 with oldest year in the db & make sure it's >= 2005
                    // FIXME don't do anything if oldest year in the db is (today.year - 1 - 4)+
                    //fetchRates(today.year - 1 - 4)
                    //fetchRates(today.year - 1 - 3)
                    //fetchRates(today.year - 1 - 2)
                    //etchRates(today.year - 1 - 1)
                }
                today.minusYears(5).format(DateTimeFormatter.ISO_LOCAL_DATE)
            }
            12 -> today.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            1, 6 -> today.minusMonths(monthsCount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> today.minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

        return db.rateDao().getRates(currencyId, since)
    }

    /**
     * Get all rates for the specified interval.
     *
     * **Don't call this on the main UI thread!**
     *
     * @param interval
     *     1 => 1 day (latest rates)
     *     10 => 10 days (last 10 days)
     *     2005+ => year 2005+
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
            isFetching.postValue(false)
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

        isFetching.postValue(false)
    }
}