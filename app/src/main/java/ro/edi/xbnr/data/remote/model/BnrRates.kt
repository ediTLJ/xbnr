package ro.edi.xbnr.data.remote.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "DataSet")
data class BnrRates(
    @Path("Body/Cube")
    @Attribute
    val date: String?,

    @Path("Body/Cube")
    @Element
    val currencies: List<BnrCurrency>?
)
