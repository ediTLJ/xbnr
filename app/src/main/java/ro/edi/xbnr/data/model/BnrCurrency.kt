package ro.edi.xbnr.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BnrCurrency(
    var code: String,
    var factor: Int = 0,
    var rate: Double = 0.0
)
