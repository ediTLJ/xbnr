package ro.edi.xbnr.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class DbCurrency(
    @PrimaryKey val id: Int,
    val code: String,
    val factor: Int = 0,
    @ColumnInfo(name = "is_favorite", index = true) var isFavorite: Boolean = false
)
