package ro.edi.xbnr.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.CurrencyRate
import ro.edi.xbnr.model.DateRate

@Dao
abstract class RateDao : BaseDao<DbRate> {
    @Transaction
    @Query("SELECT currency_id, code, multiplier, is_starred, date, rate FROM rates LEFT OUTER JOIN currencies ON rates.currency_id = currencies.id WHERE date = (SELECT MAX(date) FROM rates) ORDER BY is_starred DESC, code ASC")
    protected abstract fun query(): LiveData<List<Currency>>

    @Query("SELECT currency_id, rate FROM rates WHERE date = (SELECT MAX(date) AS date FROM rates WHERE date < (SELECT MAX(date) FROM rates))")
    protected abstract fun queryPrevious(): LiveData<List<CurrencyRate>>

    @Query("SELECT id, date, rate FROM rates WHERE currency_id = :id ORDER BY date DESC LIMIT :count")
    protected abstract fun query(id: Int, count: Int): LiveData<List<DateRate>>

    @Query("SELECT id, date, rate FROM rates WHERE currency_id = :id AND date = (SELECT MAX(date) FROM rates)")
    protected abstract fun query(id: Int): LiveData<DateRate>

    @Query("SELECT MAX(date) FROM rates")
    abstract fun getLatestDate(): String?

    /**
     * Get latest rates for all currencies.
     */
    fun getRates(): LiveData<List<Currency>> = query().getDistinct()

    /**
     * Get previous rates for all currencies.
     */
    fun getPreviousRates(): LiveData<List<CurrencyRate>> = queryPrevious().getDistinct()

    /**
     * Get latest [count] rates for the specified currency id.
     *
     * @param id currency id
     * @param count days count
     */
    fun getRates(id: Int, count: Int): LiveData<List<DateRate>> = query(id, count).getDistinct()

    /**
     * Get latest rate for specified currency.
     *
     * @param id currency id
     */
    fun getRate(id: Int): LiveData<DateRate> = query(id).getDistinct()

    @Query("DELETE FROM rates")
    abstract fun deleteAll()
}
