package ro.edi.xbnr.model

import androidx.room.ColumnInfo

data class CurrencyMinimal(
    val id: Int,
    val code: String,
    val multiplier: Int = 1,
    @ColumnInfo(name = "is_starred", index = true) var isStarred: Boolean = false
)
