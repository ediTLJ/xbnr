package ro.edi.xbnr.data

import retrofit2.Call
import retrofit2.http.GET
import ro.edi.xbnr.data.model.BnrRates

interface BnrService {
    @get:GET("rates/latest")
    val latestRates: Call<BnrRates>
}