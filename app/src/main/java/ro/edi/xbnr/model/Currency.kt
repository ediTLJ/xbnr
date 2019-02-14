package ro.edi.xbnr.model

import androidx.room.ColumnInfo

data class Currency(
    @ColumnInfo(name = "currency_id") val id: Int,
    val code: String,
    val multiplier: Int,
    @ColumnInfo(name = "is_favorite", index = true) var isFavorite: Boolean = false,
    val date: String,
    val rate: Double
)
