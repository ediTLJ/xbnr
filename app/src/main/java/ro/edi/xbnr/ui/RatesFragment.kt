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
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentRatesBinding
import ro.edi.xbnr.ui.adapter.RatesAdapter
import ro.edi.xbnr.ui.viewmodel.RatesViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRatesBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            model = ratesModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // logi("onViewCreated: %s", savedInstanceState)

        val rvRates = view.findViewById<RecyclerView>(R.id.rates)

        activity?.apply {
            val toolbar = findViewById<Toolbar>(R.id.toolbar)

            rvRates.apply {
                ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                    updatePadding(
                        insets.systemWindowInsetLeft,
                        toolbar.layoutParams.height + insets.systemWindowInsetTop,
                        insets.systemWindowInsetRight,
                        insets.systemWindowInsetBottom
                    )
                    insets
                }
            }
        }

        rvRates.apply {
            setHasFixedSize(true)
            adapter = RatesAdapter(ratesModel).apply {
                setHasStableIds(true)
            }
        }

        val colorPrimary = ContextCompat.getColor(
            view.context,
            getColorRes(view.context, R.attr.colorPrimary)
        )
        val textColorSecondary = ContextCompat.getColor(
            view.context,
            getColorRes(view.context, android.R.attr.textColorSecondary)
        )

        val pbLoading = view.findViewById<ContentLoadingProgressBar>(R.id.loading)
        val tvEmpty = view.findViewById<TextView>(R.id.empty)

        ratesModel.fetchingData.observe(viewLifecycleOwner, Observer { fetchingData ->
            logi("ratesModel fetchingData changed to %b", fetchingData)

            if (fetchingData) {
                pbLoading.show()
            } else if (pbLoading.isShown) {
                pbLoading.hide()

                if (ratesModel.currencies.value.isNullOrEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvRates.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvRates.visibility = View.VISIBLE
                }
            }
        })

        ratesModel.currencies.observe(viewLifecycleOwner, Observer { ratesList ->
            logi("ratesModel currencies changed")

            if (ratesList.isEmpty()) {
                tvEmpty.visibility =
                    if (ratesModel.fetchingData.value == false) View.VISIBLE else View.GONE
                rvRates.visibility = View.GONE
            } else {
                pbLoading.hide()
                tvEmpty.visibility = View.GONE
                rvRates.visibility = View.VISIBLE

                val layoutManager = rvRates.layoutManager as LinearLayoutManager
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                val offset = (layoutManager.findViewByPosition(firstVisible)?.top
                    ?: 0) - layoutManager.paddingTop

                (rvRates.adapter as RatesAdapter).submitList(ratesList) {
                    if (firstVisible != RecyclerView.NO_POSITION) {
                        layoutManager.scrollToPositionWithOffset(
                            firstVisible,
                            offset
                        )

                        ViewCompat.requestApplyInsets(view)
                    }
                }

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

            val payload = mutableSetOf<String>()
            payload.add(RatesAdapter.CURRENCY_DATE)
            (rvRates.adapter as RatesAdapter).notifyItemRangeChanged(0, it.size, payload)
        })

        ViewCompat.requestApplyInsets(view)
    }
}