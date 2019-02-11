package ro.edi.xbnr.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BnrCurrency(
    val code: String,
    val factor: Int = 0,
    val rate: Double
)
