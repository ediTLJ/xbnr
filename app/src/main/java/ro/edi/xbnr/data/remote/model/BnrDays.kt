package ro.edi.xbnr.data.remote.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "DataSet")
data class BnrDays(
    @Path("Body")
    @Element
    val ratesList: List<BnrRates>?
)
