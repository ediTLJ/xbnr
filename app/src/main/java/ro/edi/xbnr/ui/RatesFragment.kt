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
package ro.edi.xbnr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentRatesBinding
import ro.edi.xbnr.ui.adapter.RatesAdapter
import ro.edi.xbnr.ui.viewmodel.RatesViewModel
import timber.log.Timber.Forest.i as logi

class RatesFragment : Fragment() {
    companion object {
        fun newInstance() = RatesFragment()
    }

    private val ratesModel: RatesViewModel by viewModels()

    private var _binding: FragmentRatesBinding? = null

    // this property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatesBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = ratesModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // logi("onViewCreated: $savedInstanceState")

        binding.refresh.apply {
            // FIXME find a better way
            activity?.let {
                val toolbar = it.findViewById<Toolbar>(R.id.toolbar)

                // FIXME temp
                binding.refresh.setProgressViewEndTarget(
                    true,
                    (2.5 * toolbar.layoutParams.height).toInt()
                )
            }

            setColorSchemeResources(getColorRes(view.context, R.attr.colorPrimaryVariant))
            setOnRefreshListener {
                ratesModel.refresh()
            }
        }

        binding.rates.apply {
            // FIXME find a better way?
            activity?.let {
                val toolbar = it.findViewById<Toolbar>(R.id.toolbar)

                binding.rates.apply {
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

            setHasFixedSize(true)
            adapter = RatesAdapter(ratesModel).apply {
                setHasStableIds(true)
            }
        }

        val colorPrimary = view.context.getColor(getColorRes(view.context, R.attr.colorPrimary))
        val textColorSecondary =
            view.context.getColor(getColorRes(view.context, android.R.attr.textColorSecondary))

        ratesModel.isFetching.observe(viewLifecycleOwner) { isFetching ->
            logi("ratesModel isFetching changed to $isFetching")

            if (isFetching) {
                binding.refresh.isRefreshing = true
            } else { // else if (binding.refresh.isShown)
                binding.refresh.isRefreshing = false

                if (ratesModel.currencies.value.isNullOrEmpty()) {
                    logi("no currencies => show empty message")
                    binding.empty.visibility = View.VISIBLE
                    binding.rates.visibility = View.GONE
                } else {
                    logi("we have currencies! => show them")
                    binding.empty.visibility = View.GONE
                    binding.rates.visibility = View.VISIBLE
                }
            }
        }

        ratesModel.currencies.observe(viewLifecycleOwner) { ratesList ->
            logi("ratesModel currencies changed")

            if (ratesList.isEmpty()) {
                binding.empty.visibility =
                    if (ratesModel.isFetching.value == false) View.VISIBLE else View.GONE
                binding.rates.visibility = View.GONE
            } else {
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.VISIBLE

                val rvAdapter = binding.rates.adapter as RatesAdapter
                val llManager = binding.rates.layoutManager as LinearLayoutManager
                val firstVisible = llManager.findFirstVisibleItemPosition()
                val offset =
                    (llManager.findViewByPosition(firstVisible)?.top ?: 0) - llManager.paddingTop

                rvAdapter.submitList(ratesList) {
                    if (firstVisible != RecyclerView.NO_POSITION) {
                        llManager.scrollToPositionWithOffset(
                            firstVisible,
                            offset
                        )

                        ViewCompat.requestApplyInsets(view)
                    }

                    activity?.let {
                        val tvDate = it.findViewById<TextView>(R.id.toolbar_date)

                        val txtDate = ratesModel.getCurrencyDisplayDate(0)

                        // FIXME make it green if these are the latest rates?
                        if (tvDate.text.isNullOrEmpty() || tvDate.text == txtDate) {
                            tvDate.setTextColor(textColorSecondary)
                        } else {
                            tvDate.setTextColor(colorPrimary)
                        }

                        tvDate.text = txtDate
                    }
                }
            }
        }

        ratesModel.previousRates.observe(viewLifecycleOwner) {
            logi("ratesModel previous rates changed")

            val payload = mutableSetOf<String>()
            payload.add(RatesAdapter.CURRENCY_DATE)

            val rvAdapter = binding.rates.adapter as RatesAdapter
            rvAdapter.notifyItemRangeChanged(0, it.size, payload)
        }

        ViewCompat.requestApplyInsets(view)
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }
}