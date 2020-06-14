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
import ro.edi.util.AppExecutors
import ro.edi.util.Singleton
import ro.edi.xbnr.data.db.AppDatabase
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.data.remote.BnrService
import ro.edi.xbnr.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

            val latestDbDate = LocalDate.parse(db.rateDao().getLatestDate())
                .also { logi("latest date: %s", it) }

            val zoneIdRomania = ZoneId.of("Europe/Bucharest")
            val today = LocalDate.now(zoneIdRomania)
                .also { logi("today: %s", it) }

            var fetchedCurrentYear = false

            if (latestDbDate.isBefore(today.minusDays(10))) {
                logi("latest data is older than 10 days")

                // we should have taken weekends into accounts and if it's weekend today or if it's Monday before 1pm
                // but let's keep it like this, so we can avoid any potential gaps

                for (year in latestDbDate.year..today.year) {
                    isFetching.postValue(true)
                    fetchRates(year)
                }
                fetchedCurrentYear = true
            } else {
                val todayDayOfWeek = today.dayOfWeek
                val previousWorkday =
                    when (todayDayOfWeek) {
                        DayOfWeek.MONDAY -> {
                            today.minusDays(3)
                        }
                        DayOfWeek.SUNDAY -> {
                            today.minusDays(2)
                        }
                        else -> {
                            today.minusDays(1)
                        }
                    }

                if (latestDbDate.isBefore(previousWorkday)) {
                    logi("latest data is older than a day")

                    // if it's weekend today or it's Monday before 1pm, only 1 day needs to be fetched
                    // but let's grab 10 days, so we can avoid any potential gaps

                    fetchRates(10)
                } else if (latestDbDate.isEqual(previousWorkday)) {
                    logi("latest data is from last workday")

                    if (todayDayOfWeek == DayOfWeek.SATURDAY || todayDayOfWeek == DayOfWeek.SUNDAY) {
                        // no rates published on weekends
                        logi("weekend => nothing to do")
                        isFetching.postValue(false)
                    } else {
                        val now = LocalTime.now(zoneIdRomania)
                            .also { logi("now: %s", it) }
                        val hour1pm = LocalTime.of(13, 0)

                        if (now.isAfter(hour1pm)) {
                            logi("after 1pm => fetch today's data")
                            fetchRates(1)
                        } else {
                            // no rates published before 1pm
                            logi("before 1pm => nothing to do")
                            isFetching.postValue(false)
                        }
                    }
                } else { // today
                    logi("latest data is from today => nothing to do")
                    isFetching.postValue(false)
                }
            }

            // fetch any missing oldest data... we assume there's no data gap, though
            // this is for older app versions, that didn't have a pre-populated database with 2005-2020 rates
            val oldestDbDate = LocalDate.parse(db.rateDao().getOldestDate())
                .also { logi("oldest date: %s", it) }
            if (oldestDbDate.isAfter(LocalDate.of(2005, 1, 3))) {
                logi("oldest data is after 2015-01-03")

                // FIXME get the rates stored in assets/database, instead of getting them from the server
                //   and drop fetchedCurrentYear, we can get the stored rates up to oldestDbDate

                val toYear =
                    if (oldestDbDate.year == today.year && fetchedCurrentYear) {
                        oldestDbDate.year - 1
                    } else {
                        oldestDbDate.year
                    }

                for (year in 2005..toYear) {
                    isFetching.postValue(true)
                    fetchRates(year)
                }
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

    fun getDayRates(currencyId: Int, monthsCount: Int): LiveData<List<DayRate>> {
        val zoneIdRomania = ZoneId.of("Europe/Bucharest")
        val today = LocalDate.now(zoneIdRomania)

        val since = when (monthsCount) {
            12 -> today.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            1, 6 -> today.minusMonths(monthsCount.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> today.minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

        return db.rateDao().getDayRates(currencyId, since)
    }

    fun getMonthRates(currencyId: Int, monthsCount: Int): LiveData<List<MonthRate>> {
        val zoneIdRomania = ZoneId.of("Europe/Bucharest")
        val today = LocalDate.now(zoneIdRomania)

        // we only have a 5Y option in the app, so far...
        val since = when (monthsCount) {
            60 -> today.minusYears(5).withDayOfMonth(1).minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
            else -> today.minusYears(5).withDayOfMonth(1).minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

        return db.rateDao().getMonthRates(currencyId, since)
    }

    fun getYearRates(currencyId: Int): LiveData<List<YearRate>> {
        return db.rateDao().getYearRates(currencyId)
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