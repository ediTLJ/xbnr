package ro.edi.xbnr.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BnrRates(
    var date: String?,
    var currencies: List<BnrCurrency>?
)
