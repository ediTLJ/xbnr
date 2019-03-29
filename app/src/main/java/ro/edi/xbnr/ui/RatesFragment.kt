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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentRatesBinding
import ro.edi.xbnr.ui.adapter.RatesAdapter
import ro.edi.xbnr.ui.viewmodel.RatesViewModel
import timber.log.Timber.d as logd
import timber.log.Timber.i as logi

class RatesFragment : Fragment() {
    companion object {
        fun newInstance() = RatesFragment()
    }

    private lateinit var ratesModel: RatesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ratesModel = ViewModelProviders.of(this).get(RatesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =
            DataBindingUtil.inflate<FragmentRatesBinding>(inflater, R.layout.fragment_rates, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val ratesAdapter = RatesAdapter(ratesModel)
        ratesAdapter.setHasStableIds(true)

        val colorPrimary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, R.attr.colorPrimary)
        )
        val textColorSecondary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, android.R.attr.textColorSecondary)
        )

        ratesModel.currencies.observe(viewLifecycleOwner, Observer { list ->
            logi("ratesModel currencies changed")

            // TODO replace with databinding
            if (list == null) {
                binding.loading.show()
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.GONE
            } else if (list.isEmpty()) {
                binding.loading.hide()
                binding.empty.visibility = View.VISIBLE
                binding.rates.visibility = View.GONE
            } else {
                binding.loading.hide()
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.VISIBLE

                ratesAdapter.notifyDataSetChanged()

                activity?.apply {
                    val tvDate = findViewById<TextView>(R.id.toolbar_date)

                    val txtDate = ratesModel.getCurrencyDisplayDate(0)

                    // TODO make it green if these are the latest rates?
                    if (tvDate.text.isNullOrEmpty() || tvDate.text == txtDate) {
                        tvDate.setTextColor(textColorSecondary)
                    } else {
                        tvDate.setTextColor(colorPrimary)
                    }

                    tvDate.text = txtDate
                }
            }
        })

        ratesModel.previousRates.observe(viewLifecycleOwner, Observer {
            logi("ratesModel previous rates changed")
            ratesAdapter.notifyDataSetChanged()
        })

        with(binding.rates) {
            setHasFixedSize(true)
            adapter = ratesAdapter
        }

        return binding.root
    }
}