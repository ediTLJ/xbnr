package ro.edi.xbnr.data

import retrofit2.Call
import retrofit2.http.GET

interface BnrService {
    @get:GET("rates/latest")
    val latestRates: Call<Rates>
}