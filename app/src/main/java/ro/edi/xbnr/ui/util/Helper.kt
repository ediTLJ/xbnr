package ro.edi.xbnr.ui.util

import ro.edi.xbnr.R

object Helper {

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
}
