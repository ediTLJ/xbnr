package ro.edi.xbnr.data.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ro.edi.xbnr.data.db.dao.CurrencyDao
import ro.edi.xbnr.data.db.dao.RateDao
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate
import ro.edi.xbnr.util.Singleton

@Database(entities = [DbCurrency::class, DbRate::class], version = 1, exportSchema = false)
abstract class RatesDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun rateDao(): RateDao

    companion object : Singleton<RatesDatabase, Application>({
        Room.databaseBuilder(it, RatesDatabase::class.java, "rates.db")
            .fallbackToDestructiveMigration().build()
    })
}
