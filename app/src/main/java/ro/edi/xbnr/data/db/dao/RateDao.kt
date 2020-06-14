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
package ro.edi.xbnr.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.model.*

@Dao
abstract class RateDao : BaseDao<DbRate> {
    @Transaction
    @Query("SELECT currency_id, code, multiplier, is_starred, date, rate FROM rates LEFT OUTER JOIN currencies ON rates.currency_id = currencies.id WHERE date = (SELECT max(date) FROM rates) ORDER BY is_starred DESC, code ASC")
    protected abstract fun query(): LiveData<List<Currency>>

    @Transaction
    @Query("SELECT currency_id, rate FROM rates WHERE date = (SELECT max(date) AS date FROM rates WHERE date < (SELECT max(date) FROM rates))")
    protected abstract fun queryPrevious(): LiveData<List<Rate>>

    @Query("SELECT currency_id, code, multiplier, is_starred, date, rate FROM rates LEFT OUTER JOIN currencies ON rates.currency_id = currencies.id WHERE currency_id = :id AND date = (SELECT max(date) FROM rates)")
    protected abstract fun query(id: Int): LiveData<Currency>

    @Query("SELECT currency_id, code, multiplier, is_starred, date, rate FROM rates LEFT OUTER JOIN currencies ON rates.currency_id = currencies.id WHERE currency_id = :id AND date = :date")
    protected abstract fun query(id: Int, date: String): LiveData<Currency>

    @Transaction
    @Query("SELECT id, date, rate FROM (SELECT id, date, rate FROM rates WHERE currency_id = :id ORDER BY date DESC LIMIT :count) ORDER BY date ASC")
    protected abstract fun queryDayRates(id: Int, count: Int): LiveData<List<DayRate>>

    @Transaction
    @Query("SELECT id, date, rate FROM (SELECT id, date, rate FROM rates WHERE currency_id = :id AND date > :since) ORDER BY date ASC")
    protected abstract fun queryDayRates(id: Int, since: String): LiveData<List<DayRate>>

    @Transaction
    @Query("SELECT id, date, rate FROM (SELECT id, date, rate FROM rates WHERE currency_id = :id) ORDER BY date ASC")
    protected abstract fun queryDayRates(id: Int): LiveData<List<DayRate>>

    @Transaction
    @Query(
        "WITH\n" +
            "\n" +
            "q1 AS (SELECT\n" +
            "  id,\n" +
            "  strftime('%Y-%m', date) AS month,\n" +
            "  (SELECT rate FROM rates AS q WHERE q.date = min(rates.date) AND currency_id = :id AND date > :since GROUP BY strftime('%Y-%m', q.date)) AS open,\n" +
            "  (SELECT rate FROM rates AS q WHERE q.date = max(rates.date) AND currency_id = :id AND date > :since GROUP BY strftime('%Y-%m', q.date)) AS close\n" +
            "FROM rates WHERE currency_id = :id AND date > :since\n" +
            "GROUP BY month\n" +
            "ORDER BY date ASC),\n" +
            "\n" +
            "q2 AS (SELECT\n" +
            "  id,\n" +
            "  strftime('%Y-%m', date) AS month,\n" +
            "  min(rate) AS min,\n" +
            "  max(rate) AS max\n" +
            "FROM rates WHERE currency_id = :id AND date > :since\n" +
            "GROUP BY month\n" +
            "ORDER BY date ASC)\n" +
            "\n" +
            "SELECT\n" +
            "  q1.id,\n" +
            "  q1.month,\n" +
            "  q1.open,\n" +
            "  q1.close,\n" +
            "  q2.min,\n" +
            "  q2.max\n" +
            "FROM q1 JOIN q2 ON q1.month=q2.month\n" +
            "ORDER BY q1.month ASC"
    )
    protected abstract fun queryMonthRates(id: Int, since: String): LiveData<List<MonthRate>>

    @Transaction
    @Query(
        "WITH\n" +
            "\n" +
            "q1 AS (SELECT\n" +
            "  id,\n" +
            "  strftime('%Y', date) AS year,\n" +
            "  (SELECT rate FROM rates AS q WHERE q.date = min(rates.date) AND currency_id = :id GROUP BY strftime('%Y', q.date)) AS open,\n" +
            "  (SELECT rate FROM rates AS q WHERE q.date = max(rates.date) AND currency_id = :id GROUP BY strftime('%Y', q.date)) AS close\n" +
            "FROM rates WHERE currency_id = :id\n" +
            "GROUP BY year\n" +
            "ORDER BY date ASC),\n" +
            "\n" +
            "q2 AS (SELECT\n" +
            "  id,\n" +
            "  strftime('%Y', date) AS year,\n" +
            "  min(rate) AS min,\n" +
            "  max(rate) AS max\n" +
            "FROM rates WHERE currency_id = :id\n" +
            "GROUP BY year\n" +
            "ORDER BY date ASC)\n" +
            "\n" +
            "SELECT\n" +
            "  q1.id,\n" +
            "  q1.year,\n" +
            "  q1.open,\n" +
            "  q1.close,\n" +
            "  q2.min,\n" +
            "  q2.max\n" +
            "FROM q1 JOIN q2 ON q1.year=q2.year\n" +
            "ORDER BY q1.year ASC"
    )
    protected abstract fun queryYearRates(id: Int): LiveData<List<YearRate>>

    @Query("SELECT min(date) FROM rates")
    abstract fun getOldestDate(): String?

    @Query("SELECT max(date) FROM rates")
    abstract fun getLatestDate(): String?

    /**
     * Get latest rates for all currencies.
     */
    fun getRates(): LiveData<List<Currency>> = query().getDistinct()

    /**
     * Get previous rates for all currencies.
     */
    fun getPreviousRates(): LiveData<List<Rate>> = queryPrevious().getDistinct()

    /**
     * Get currency info & latest rate.
     *
     * @param id currency id
     */
    fun getCurrency(id: Int): LiveData<Currency> = query(id).getDistinct()

    /**
     * Get currency info & rate for a specific date.
     *
     * @param id currency id
     */
    fun getCurrency(id: Int, date: String): LiveData<Currency> = query(id, date).getDistinct()

    /**
     * Get latest [count] rates for the specified currency.
     *
     * @param id currency id
     * @param count days count
     */
    fun getDayRates(id: Int, count: Int): LiveData<List<DayRate>> =
        queryDayRates(id, count).getDistinct()

    /**
     * Get rates since [since] for the specified currency.
     *
     * @param id currency id
     * @param since date
     */
    fun getDayRates(id: Int, since: String): LiveData<List<DayRate>> =
        queryDayRates(id, since).getDistinct()

    /**
     * Get all rates for the specified currency.
     *
     * @param id currency id
     */
    fun getDayRates(id: Int): LiveData<List<DayRate>> =
        queryDayRates(id).getDistinct()

    /**
     * Get month rates since [since] for the specified currency.
     *
     * @param id currency id
     * @param since date
     */
    fun getMonthRates(id: Int, since: String): LiveData<List<MonthRate>> =
        queryMonthRates(id, since).getDistinct()

    /**
     * Get all year rates for the specified currency.
     *
     * @param id currency id
     */
    fun getYearRates(id: Int): LiveData<List<YearRate>> =
        queryYearRates(id).getDistinct()

    @Query("DELETE FROM rates")
    abstract fun deleteAll()
}