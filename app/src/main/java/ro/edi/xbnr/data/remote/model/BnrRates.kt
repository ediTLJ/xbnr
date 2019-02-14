package ro.edi.xbnr.data.remote.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "Cube")
data class BnrRates(
    @Attribute
    val date: String?,

    @Element
    val currencies: List<BnrCurrency>?
)
