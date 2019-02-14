package ro.edi.xbnr.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.model.RateMinimal

@Dao
abstract class RateDao : BaseDao<DbRate> {
    @Transaction
    @Query("SELECT currency_id, code, multiplier, is_favorite, date, rate FROM rates LEFT OUTER JOIN currencies ON (rates.currency_id = currencies.id) WHERE date = (SELECT MAX(date) FROM rates) ORDER BY code ASC")
    protected abstract fun query(): LiveData<List<Currency>>

    @Transaction
    @Query("SELECT id, date, rate FROM rates WHERE currency_id = :id ORDER BY date DESC LIMIT :count")
    protected abstract fun query(id: Int, count: Int): LiveData<List<RateMinimal>>

    @Transaction
    @Query("SELECT id, date, rate FROM rates WHERE currency_id = :id AND date = (SELECT MAX(date) FROM rates)")
    protected abstract fun query(id: Int): LiveData<RateMinimal>

    /**
     * Get latest rates for all currencies.
     */
    fun getRates(): LiveData<List<Currency>> = query().getDistinct()

    /**
     * Get latest [count] rates for the specified currency id.
     *
     * @param id currency id
     * @param count days count
     */
    fun getRates(id: Int, count: Int): LiveData<List<RateMinimal>> = query(id, count).getDistinct()

    /**
     * Get latest rate for specified currency.
     *
     * @param id currency id
     */
    fun getRate(id: Int): LiveData<RateMinimal> = query(id).getDistinct()

    @Query("DELETE FROM rates")
    abstract fun deleteAll()
}
