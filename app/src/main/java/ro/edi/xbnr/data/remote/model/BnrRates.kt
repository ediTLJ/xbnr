package ro.edi.xbnr.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BnrRates(
    val date: String?,
    val currencies: List<BnrCurrency>?
)
