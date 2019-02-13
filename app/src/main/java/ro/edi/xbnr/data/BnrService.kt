package ro.edi.xbnr.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import ro.edi.xbnr.data.model.BnrRates

interface BnrService {
    @get:GET("rates/latest")
    val latestRates: Call<BnrRates>

    companion object {
        private const val API_BASE_URL = "https://xbnr-api.herokuapp.com/"
        // private const val API_KEY = "whatever" // FIXME

        val instance: BnrService by lazy {
            val clientBuilder = OkHttpClient.Builder()

            // add other interceptors here

            // add logging as last interceptor
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            clientBuilder.addInterceptor(logging)

            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(clientBuilder.build())
                .build()
            retrofit.create(BnrService::class.java)
        }
    }
}
