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
package ro.edi.xbnr.data.db.entity

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "rates",
    indices = [Index(value = ["currency_id", "date"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = DbCurrency::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("currency_id"),
        onDelete = CASCADE
    )]
)
data class DbRate(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "currency_id") val currencyId: Int,
    val date: String,
    val rate: Double
)