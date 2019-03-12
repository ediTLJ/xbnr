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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.model.DateRate
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
        binding.lifecycleOwner = viewLifecycleOwner

        val colorAccent = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, R.attr.colorAccent)
        )
        val textColorPrimary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, android.R.attr.textColorPrimary)
        )
        val textColorSecondary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, android.R.attr.textColorSecondary)
        )

        binding.chart.apply {
            isAutoScaleMinMaxEnabled = true
            isKeepPositionOnRotation = true
            legend.isEnabled = false
            description.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            isHighlightPerTapEnabled = true

            setNoDataText(getString(R.string.no_data_found))
            setNoDataTextColor(textColorSecondary)
            ResourcesCompat.getFont(context, R.font.fira_sans_condensed_medium)?.let {
                setNoDataTextTypeface(it)
            }

            xAxis.valueFormatter = DayAxisFormatter(historyModel.rates)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.setLabelCount(20, true)
            xAxis.textColor = textColorPrimary
            xAxis.textSize = 12f
            ResourcesCompat.getFont(context, R.font.fira_sans_condensed_regular)?.let {
                xAxis.typeface = it
            }

            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            val clickListener = object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    show(e.data as DateRate)
                }

                override fun onNothingSelected() {
                    historyModel.rates.value?.lastOrNull()?.let {
                        show(it)
                    }
                }

                private fun show(rate: DateRate) {
                    activity?.run {
                        findViewById<TextView>(R.id.currency_date).text =
                            LocalDate.parse(rate.date).format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                            )

                        findViewById<TextView>(R.id.currency_value).text = String.format("%.4f", rate.rate)
                    }
                }
            }
            setOnChartValueSelectedListener(clickListener)

            setVisibleXRangeMaximum(20f)
            setExtraOffsets(0f, 0f, 0f, 4f)
        }

        historyModel.rates.observe(viewLifecycleOwner, Observer { rates ->
            logi("ratesModel currencies changed")

            if (rates.isNullOrEmpty()) {
                binding.loading.show()
                binding.chart.visibility = View.GONE
            } else {
                binding.loading.hide()

                val entries = mutableListOf<Entry>()

                rates.forEachIndexed { index, rate ->
                    entries.add(Entry(index.toFloat(), rate.rate.toFloat(), rate))
                }

                val dataSet = LineDataSet(entries, "rates").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    mode = LineDataSet.Mode.STEPPED

                    lineWidth = 2.0f
                    setDrawCircles(false)
                    // setCircleColor(colorAccent)
                    // circleHoleColor = colorAccent
                    // circleRadius = 5.0f

                    setDrawFilled(true)
                    fillColor = colorAccent
                    fillAlpha = 25

                    setDrawHorizontalHighlightIndicator(false)
                    setDrawVerticalHighlightIndicator(true)
                    highLightColor = textColorPrimary
                    highlightLineWidth = 2.0f

                    setDrawValues(false)
                    color = colorAccent
                }

                binding.chart.apply {
                    data = LineData(dataSet)
                    notifyDataSetChanged()

                    visibility = View.VISIBLE
                    animateX(500, Easing.EaseOutBack)
                }
            }
        })

        return binding.root
    }

    class DayAxisFormatter(private val rates: LiveData<List<DateRate>>) : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return rates.value?.getOrNull(value.toInt())?.date?.takeLast(2) ?: ""
        }
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