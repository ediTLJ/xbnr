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
package ro.edi.xbnr.ui.util

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import ro.edi.xbnr.R

object Helper {
    fun setTheme(theme: String?) {
        val mode: Int = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            } else {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun getCurrencyIconRes(code: String?): Int {
        return when (code) {
            "AED" -> R.drawable.ic_flag_aed
            "AUD" -> R.drawable.ic_flag_aud
            "BGN" -> R.drawable.ic_flag_bgn
            "BRL" -> R.drawable.ic_flag_brl
            "CAD" -> R.drawable.ic_flag_cad
            "CHF" -> R.drawable.ic_flag_chf
            "CNY" -> R.drawable.ic_flag_cny
            "CZK" -> R.drawable.ic_flag_czk
            "DKK" -> R.drawable.ic_flag_dkk
            "EGP" -> R.drawable.ic_flag_egp
            "EUR" -> R.drawable.ic_flag_eur
            "GBP" -> R.drawable.ic_flag_gbp
            "HRK" -> R.drawable.ic_flag_hrk
            "HUF" -> R.drawable.ic_flag_huf
            "INR" -> R.drawable.ic_flag_inr
            "JPY" -> R.drawable.ic_flag_jpy
            "KRW" -> R.drawable.ic_flag_krw
            "MDL" -> R.drawable.ic_flag_mdl
            "MXN" -> R.drawable.ic_flag_mxn
            "NOK" -> R.drawable.ic_flag_nok
            "NZD" -> R.drawable.ic_flag_nzd
            "PLN" -> R.drawable.ic_flag_pln
            "RSD" -> R.drawable.ic_flag_rsd
            "RUB" -> R.drawable.ic_flag_rub
            "SEK" -> R.drawable.ic_flag_sek
            "THB" -> R.drawable.ic_flag_thb
            "TRY" -> R.drawable.ic_flag_try
            "UAH" -> R.drawable.ic_flag_uah
            "USD" -> R.drawable.ic_flag_usd
            "XAU" -> R.drawable.ic_flag_xau
            "XDR" -> R.drawable.ic_flag_xdr
            "ZAR" -> R.drawable.ic_flag_zar
            "RON" -> R.drawable.ic_flag_ron
            else -> R.drawable.ic_flag_default
        }
    }

    fun getCurrencyNameRes(code: String?): Int {
        return when (code) {
            "AED" -> R.string.name_aed
            "AUD" -> R.string.name_aud
            "BGN" -> R.string.name_bgn
            "BRL" -> R.string.name_brl
            "CAD" -> R.string.name_cad
            "CHF" -> R.string.name_chf
            "CNY" -> R.string.name_cny
            "CZK" -> R.string.name_czk
            "DKK" -> R.string.name_dkk
            "EGP" -> R.string.name_egp
            "EUR" -> R.string.name_eur
            "GBP" -> R.string.name_gbp
            "HRK" -> R.string.name_hrk
            "HUF" -> R.string.name_huf
            "INR" -> R.string.name_inr
            "JPY" -> R.string.name_jpy
            "KRW" -> R.string.name_krw
            "MDL" -> R.string.name_mdl
            "MXN" -> R.string.name_mxn
            "NOK" -> R.string.name_nok
            "NZD" -> R.string.name_nzd
            "PLN" -> R.string.name_pln
            "RSD" -> R.string.name_rsd
            "RUB" -> R.string.name_rub
            "SEK" -> R.string.name_sek
            "THB" -> R.string.name_thb
            "TRY" -> R.string.name_try
            "UAH" -> R.string.name_uah
            "USD" -> R.string.name_usd
            "XAU" -> R.string.name_xau
            "XDR" -> R.string.name_xdr
            "ZAR" -> R.string.name_zar
            "RON" -> R.string.name_ron
            else -> R.string.name_default
        }
    }

    fun getCurrencySymbolRes(code: String?): Int {
        return when (code) {
            "AED" -> R.string.symbol_aed
            "AUD" -> R.string.symbol_aud
            "BGN" -> R.string.symbol_bgn
            "BRL" -> R.string.symbol_brl
            "CAD" -> R.string.symbol_cad
            "CHF" -> R.string.symbol_chf
            "CNY" -> R.string.symbol_cny
            "CZK" -> R.string.symbol_czk
            "DKK" -> R.string.symbol_dkk
            "EGP" -> R.string.symbol_egp
            "EUR" -> R.string.symbol_eur
            "GBP" -> R.string.symbol_gbp
            "HRK" -> R.string.symbol_hrk
            "HUF" -> R.string.symbol_huf
            "INR" -> R.string.symbol_inr
            "JPY" -> R.string.symbol_jpy
            "KRW" -> R.string.symbol_krw
            "MDL" -> R.string.symbol_mdl
            "MXN" -> R.string.symbol_mxn
            "NOK" -> R.string.symbol_nok
            "NZD" -> R.string.symbol_nzd
            "PLN" -> R.string.symbol_pln
            "RSD" -> R.string.symbol_rsd
            "RUB" -> R.string.symbol_rub
            "SEK" -> R.string.symbol_sek
            "THB" -> R.string.symbol_thb
            "TRY" -> R.string.symbol_try
            "UAH" -> R.string.symbol_uah
            "USD" -> R.string.symbol_usd
            "XAU" -> R.string.symbol_xau
            "XDR" -> R.string.symbol_xdr
            "ZAR" -> R.string.symbol_zar
            "RON" -> R.string.symbol_ron
            else -> R.string.symbol_default
        }
    }
}