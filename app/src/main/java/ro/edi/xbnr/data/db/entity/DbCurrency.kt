package ro.edi.xbnr.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class DbCurrency(
    @PrimaryKey val id: Int,
    val code: String,
    val multiplier: Int = 1,
    @ColumnInfo(name = "is_favorite", index = true) val isFavorite: Boolean = false
)
