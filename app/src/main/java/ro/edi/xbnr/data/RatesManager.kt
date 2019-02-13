package ro.edi.xbnr.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.edi.xbnr.data.model.BnrRates

object RatesManager {
    // private const val TAG = "RATES.MANAGER"

    // simple in-memory cache
    // private UserCache userCache;

    //        LiveData<User> cached = userCache.get(userId);
    //        if (cached != null) {
    //            return cached;
    //        }
    //        userCache.put(userId, data);

    init {

    }

    fun getLatestRates(): LiveData<BnrRates> {
        val data = MutableLiveData<BnrRates>()

        BnrService.instance.latestRates.enqueue(object : Callback<BnrRates> {
            override fun onResponse(call: Call<BnrRates>, response: Response<BnrRates>) {
                val rates = response.body() ?: return

                rates.date ?: return
                rates.currencies ?: return

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

//class RatesManager private constructor(context: Context) {
//    init {
//        // init using context argument
//    }
//
//    companion object : SingletonHolder<RatesManager, Context>(::RatesManager)
//}
