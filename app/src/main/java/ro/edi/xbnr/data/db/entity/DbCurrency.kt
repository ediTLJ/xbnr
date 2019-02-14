package ro.edi.xbnr.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// indices = [Index(value = ["code", "is_favorite"])]
@Entity(tableName = "currencies")
data class DbCurrency(
    @PrimaryKey val id: Int,
    val code: String,
    val multiplier: Int = 1,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)
