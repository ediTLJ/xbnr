/*
* Copyright 2019 Eduard Scarlat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.edi.xbnr.data.remote

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import ro.edi.xbnr.data.remote.model.BnrDays
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

interface BnrService {
    @get:GET("nbrfxrates.xml")
    val latestRates: Call<BnrDays>

    @get:GET("nbrfxrates10days.xml")
    val last10Rates: Call<BnrDays>

    @GET("/files/xml/years/nbrfxrates{year}.xml")
    fun rates(@Path("year") year: Int): Call<BnrDays>

    companion object {
        private const val API_BASE_URL = "https://bnr.ro/"

        val instance: BnrService by lazy {
            val okBuilder = OkHttpClient.Builder()

            // add other interceptors here

            // add logging as last interceptor
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            okBuilder.addInterceptor(logging)

            okBuilder.hostnameVerifier(object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    return hostname == "bnr.ro"
                }
            })

            val tikXml = TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(TikXmlConverterFactory.create(tikXml))
                .client(okBuilder.build())
                .build()
            retrofit.create(BnrService::class.java)
        }
    }
}