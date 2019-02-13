package ro.edi.xbnr.data.db.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

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
    @ColumnInfo(name = "currency_id") var currencyId: Int,
    val date: String,
    val rate: Double
)
