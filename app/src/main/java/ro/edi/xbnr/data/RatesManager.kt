package ro.edi.xbnr.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ro.edi.xbnr.data.model.BnrRates

object RatesManager {
    // private const val TAG = "RATES.MANAGER"

    private const val API_BASE_URL = "https://xbnr-api.herokuapp.com/"
    // private const val API_KEY = "whatever" // FIXME

    private val bnrService: BnrService

    // simple in-memory cache
    // private UserCache userCache;

    //        LiveData<User> cached = userCache.get(userId);
    //        if (cached != null) {
    //            return cached;
    //        }
    //        userCache.put(userId, data);

    init {
        val clientBuilder = OkHttpClient.Builder()

        // add other interceptors here

        // add logging as last interceptor
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        clientBuilder.addInterceptor(logging)

        bnrService = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(clientBuilder.build())
            .build()
            .create(BnrService::class.java)
    }

    fun getLatestRates(): LiveData<BnrRates> {
        val data = MutableLiveData<BnrRates>()

        bnrService.latestRates.enqueue(object : Callback<BnrRates> {
            override fun onResponse(call: Call<BnrRates>, response: Response<BnrRates>) {
                val rates = response.body() ?: return

                // FIXME check rates date
                data.value = rates
            }

            override fun onFailure(call: Call<BnrRates>, t: Throwable) {
                // FIXME show error
                data.value = BnrRates(null, null)
            }
        })
        return data
    }
}
