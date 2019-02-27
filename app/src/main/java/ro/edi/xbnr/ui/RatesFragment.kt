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
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.RatesFragmentBinding
import ro.edi.xbnr.ui.adapter.RatesAdapter
import ro.edi.xbnr.ui.viewmodel.RatesViewModel

class RatesFragment : Fragment() {
    private val ratesModel: RatesViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this).get(RatesViewModel::class.java)
    }

    companion object {
        // private const val TAG = "RATES.FRAGMENT"

        fun newInstance(): RatesFragment {
            return RatesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =
            DataBindingUtil.inflate<RatesFragmentBinding>(inflater, R.layout.rates_fragment, container, false)

        val ratesAdapter = RatesAdapter(ratesModel)
        ratesAdapter.setHasStableIds(true)

        ratesModel.currencies.observe(this, Observer {
            // TODO replace with databinding
            if (it == null) {
                binding.loading.show()
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.GONE
            } else if (it.isEmpty()) {
                binding.loading.hide()
                binding.empty.visibility = View.VISIBLE
                binding.rates.visibility = View.GONE
            } else {
                binding.loading.hide()
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.VISIBLE

                ratesAdapter.notifyDataSetChanged()

                val tvDate = (activity as MainActivity).findViewById<TextView>(R.id.toolbar_date)
                val latestDateString = ratesModel.getCurrency(0)?.date

                val latestDate = LocalDate.parse(latestDateString)
                val txtDate = latestDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

                if (tvDate.text.isNullOrEmpty() || tvDate.text == txtDate) {
                    tvDate.setTextColor(
                        ContextCompat.getColor(
                            activity as MainActivity,
                            getColorRes(activity as MainActivity, android.R.attr.textColorSecondary)
                        )
                    )
                } else {
                    tvDate.setTextColor(
                        ContextCompat.getColor(
                            activity as MainActivity,
                            getColorRes(activity as MainActivity, android.R.attr.colorPrimary)
                        )
                    )
                }

                tvDate.text = txtDate
            }
        })

        ratesModel.previousRates.observe(this, Observer {
            ratesAdapter.notifyDataSetChanged()
        })

        with(binding.rates) {
            setHasFixedSize(true)
            adapter = ratesAdapter
        }

        return binding.root
    }
}