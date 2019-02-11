package ro.edi.xbnr.data

class Currency {
    lateinit var code: String
    var factor: Int = 0
    var rate: Double = 0.toDouble()
}
