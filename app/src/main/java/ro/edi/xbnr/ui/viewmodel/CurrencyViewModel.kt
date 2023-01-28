/*
* Copyright 2019-2023 Eduard Scarlat
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
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ro.edi.xbnr.R
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.ui.util.Helper

class CurrencyViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var currencyId: Int
        get() = savedStateHandle[KEY_CURRENCY_ID] ?: 0
        set(id) {
            savedStateHandle[KEY_CURRENCY_ID] = id
        }

    val currency: LiveData<Currency> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(application).getCurrency(currencyId)
    }

    fun getCurrencyDisplayCode(): String? {
        return currency.value?.let {
            if (it.multiplier > 1) {
                application.resources.getQuantityString(
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
            DataManager.getInstance(application).update(it, isStarred)
        }
    }

    companion object {
        private const val KEY_CURRENCY_ID = "currency-id"

        val FACTORY = viewModelFactory {
            // the return type of the lambda automatically sets what class this lambda handles
            initializer {
                // get the Application object from extras provided to the lambda
                val application = checkNotNull(this[APPLICATION_KEY])

                val savedStateHandle = createSavedStateHandle()

                CurrencyViewModel(
                    application = application,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}