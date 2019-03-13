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
import com.github.mikephil.charting.data.LineDataSet
import ro.edi.xbnr.data.DataManager
import ro.edi.xbnr.model.DateRate

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private var currencyId = -1

    val rates: LiveData<List<DateRate>> by lazy(LazyThreadSafetyMode.NONE) {
        DataManager.getInstance(getApplication()).getRates(currencyId, 20)
    }

    var chartHighlightX = -1f
    var chartMode = LineDataSet.Mode.HORIZONTAL_BEZIER

    constructor(application: Application, currencyId: Int) : this(application) {
        this.currencyId = currencyId
    }
}