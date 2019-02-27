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
import ro.edi.xbnr.data.db.entity.DbCurrency
import ro.edi.xbnr.model.CurrencyMinimal

@Dao
abstract class CurrencyDao : BaseDao<DbCurrency> {
    @Query("SELECT * FROM currencies ORDER BY is_starred DESC, code ASC")
    protected abstract fun queryAll(): LiveData<List<CurrencyMinimal>>

    /**
     * Get all currencies.
     */
    fun getCurrencies(): LiveData<List<CurrencyMinimal>> = queryAll().getDistinct()

    @Query("DELETE FROM currencies")
    abstract fun deleteAll()
}