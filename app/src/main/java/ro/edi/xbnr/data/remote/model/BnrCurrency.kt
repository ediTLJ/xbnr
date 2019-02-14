package ro.edi.xbnr.data.remote.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.TextContent
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "Rate")
data class BnrCurrency(
    @Attribute(name = "currency")
    val code: String,

    @Attribute
    val multiplier: Int = 0,

    @TextContent
    val rate: String
)
