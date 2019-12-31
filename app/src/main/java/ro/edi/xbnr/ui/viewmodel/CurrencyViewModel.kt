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
package ro.edi.xbnr.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ro.edi.xbnr.R
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.ui.util.Helper

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {
    private var currencyId = 0

    val currency: LiveData<Currency> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getCurrency(currencyId)
    }

    constructor(application: Application, currencyId: Int) : this(application) {
        this.currencyId = currencyId
    }

    fun getCurrencyDisplayCode(): String? {
        return currency.value?.let {
            if (it.multiplier > 1) {
                (getApplication() as Application).resources.getQuantityString(
                    R.plurals.currency_multiplier,
                    it.multiplier,
                    it.multiplier,
                    it.code
                )
            } else it.code
        }
    }

    fun getCurrencyIconRes(): Int {
        return Helper.getCurrencyIconRes(currency.value?.code)
    }

    fun getIsStarred(): Boolean? {
        return currency.value?.isStarred
    }

    fun setIsStarred(isStarred: Boolean) {
        currency.value?.let {
            DataManager.getInstance(getApplication()).update(it, isStarred)
        }
    }
}