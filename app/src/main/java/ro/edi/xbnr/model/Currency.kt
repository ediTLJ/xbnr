package ro.edi.xbnr.model

import androidx.room.ColumnInfo

data class Currency(
    @ColumnInfo(name = "currency_id") val id: Int,
    val code: String,
    val multiplier: Int,
    @ColumnInfo(name = "is_starred", index = true) var isStarred: Boolean = false,
    val date: String,
    val rate: Double
)
