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
package ro.edi.xbnr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.ui.viewmodel.HistoryViewModel
import timber.log.Timber.i as logi

class HistoryFragment : Fragment() {
    companion object {
        const val ARG_CURRENCY_ID = "ro.edi.xbnr.ui.history.arg_currency_id"

        fun newInstance(currencyId: Int) = HistoryFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CURRENCY_ID, currencyId)
            }
        }
    }

    private val historyModel: HistoryViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, factory).get(HistoryViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =
            DataBindingUtil.inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)

        historyModel.rates.observe(this, Observer {
            logi("ratesModel currencies changed")

            if (it.isNullOrEmpty()) {
                binding.loading.show()
                binding.chart.visibility = View.GONE
            } else {
                binding.loading.hide()
                binding.chart.visibility = View.VISIBLE
            }
        })

        return binding.root
    }

    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                (activity as AppCompatActivity).application,
                arguments?.getInt(ARG_CURRENCY_ID, -1) ?: -1
            ) as T
        }
    }
}