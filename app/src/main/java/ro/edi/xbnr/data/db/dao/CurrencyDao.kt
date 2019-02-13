package ro.edi.xbnr.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.model.CurrencyMinimal

@Dao
abstract class CurrencyDao : BaseDao<DbCurrency> {
    @Query("SELECT * FROM currencies ORDER BY is_favorite DESC, code ASC")
    protected abstract fun queryAll(): LiveData<List<CurrencyMinimal>>

    /**
     * Get all currencies.
     */
    fun getCurrencies(): LiveData<List<CurrencyMinimal>> = queryAll().getDistinct()

    @Query("DELETE FROM currencies")
    abstract fun deleteAll()
}
