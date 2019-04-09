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
package ro.edi.xbnr.data.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ro.edi.util.Singleton
import ro.edi.xbnr.data.db.dao.CurrencyDao
import ro.edi.xbnr.data.db.dao.RateDao
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.data.db.entity.DbRate

@Database(entities = [DbCurrency::class, DbRate::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object : Singleton<AppDatabase, Application>({
        Room.databaseBuilder(it, AppDatabase::class.java, "rates.db")
            .fallbackToDestructiveMigration().build()
    })

    abstract fun currencyDao(): CurrencyDao

    abstract fun rateDao(): RateDao
}