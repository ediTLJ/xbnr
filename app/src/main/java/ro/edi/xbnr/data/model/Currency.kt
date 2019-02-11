package ro.edi.xbnr.data.model

class Currency {
    lateinit var code: String
    var factor: Int = 0
    var rate: Double = 0.toDouble()
}
