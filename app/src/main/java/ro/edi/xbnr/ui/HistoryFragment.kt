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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.model.DateRate
import ro.edi.xbnr.ui.viewmodel.HistoryViewModel
import ro.edi.xbnr.ui.viewmodel.PREFS_KEY_CHART_INTERVAL
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

    private lateinit var historyModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyModel = ViewModelProviders.of(this, factory).get(HistoryViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =
            DataBindingUtil.inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        val tfFiraCondensed = ResourcesCompat.getFont(binding.root.context, R.font.fira_sans_condensed)
        val tfFTitilliumWeb = ResourcesCompat.getFont(binding.root.context, R.font.titillium_web)

        val colorPrimary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, R.attr.colorPrimary)
        )
        val textColorSecondary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, android.R.attr.textColorSecondary)
        )
        val colorOrange = ContextCompat.getColor(binding.root.context, R.color.orange_300)
        val colorGreen = ContextCompat.getColor(binding.root.context, R.color.green_300)

        val bkgChart = ContextCompat.getDrawable(binding.root.context, R.drawable.bkg_chart)

        binding.lineChart.apply {
            isAutoScaleMinMaxEnabled = true
            isKeepPositionOnRotation = true
            legend.isEnabled = false
            description.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            xAxis.isEnabled = false
            axisRight.isEnabled = false

            // setVisibleXRangeMaximum(20f)
            minOffset = 8f

            axisLeft.isEnabled = true
            axisLeft.setDrawAxisLine(false)
            axisLeft.setDrawGridLines(false)
            axisLeft.setDrawLabels(false)

            val marker = MarkerImage(context, R.drawable.ic_dot)
            marker.setOffset(-10f, -10f) // drawable size + line width
            setMarker(marker)

            setNoDataText(getString(R.string.no_data_found))
            setNoDataTextColor(textColorSecondary)
            tfFiraCondensed?.let {
                setNoDataTextTypeface(it)
            }

            val clickListener = object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    val rate = e.data as DateRate
                    historyModel.chartHighlight = rate
                    show(rate)
                }

                override fun onNothingSelected() {
                    historyModel.chartHighlight = null
                    historyModel.rates.value?.lastOrNull()?.let {
                        show(it)
                    }
                }

                private fun show(rate: DateRate) {
                    activity?.run {
                        findViewById<TextView>(R.id.currency_date).text = historyModel.getDisplayDate(rate)
                        findViewById<TextView>(R.id.currency_value).text = historyModel.getDisplayRate(rate)
                    }
                }
            }
            setOnChartValueSelectedListener(clickListener)
        }

        binding.chartInterval.apply {
            val checkedId = when (sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)) {
                1 -> R.id.interval_1m
                3 -> R.id.interval_3m
                12 -> R.id.interval_1y
                else -> R.id.interval_1m
            }

            for (i in 0 until childCount) {
                val chip = getChildAt(i) as Chip
                chip.isClickable = chip.id != checkedId
                chip.isChecked = chip.id == checkedId
            }

            setOnCheckedChangeListener { chipGroup, id ->
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    chip.isClickable = chip.id != chipGroup.checkedChipId
                    chip.isChecked = chip.id == chipGroup.checkedChipId
                }

                binding.lineChart.apply {
                    val interval: Int = when (id) {
                        R.id.interval_1m -> 1
                        R.id.interval_3m -> 3
                        R.id.interval_1y -> 12
                        else -> 1
                    }

                    sharedPrefs.edit()
                        .putInt(PREFS_KEY_CHART_INTERVAL, interval)
                        .apply()
                }
            }
        }

        historyModel.rates.observe(viewLifecycleOwner, Observer { rates ->
            logi("ratesModel currencies changed")

            binding.lineChart.visibility = View.GONE
            binding.loadingContainer.visibility = View.VISIBLE
            binding.loading.visibility = View.VISIBLE

            if (rates.isNullOrEmpty()) {
                return@Observer
            }

            val entries = mutableListOf<Entry>()
            rates.forEachIndexed { index, rate ->
                entries.add(Entry(index.toFloat(), rate.rate.toFloat(), rate))
            }

            val dataSet = LineDataSet(entries, "rates").apply {
                setDrawCircles(false)
                setDrawValues(false)
                setDrawHighlightIndicators(false)

                axisDependency = YAxis.AxisDependency.LEFT
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER

                lineWidth = 2.0f
                color = colorPrimary
                setDrawFilled(true)
                fillDrawable = bkgChart
            }

            binding.lineChart.apply {
                data = LineData(dataSet)
                notifyDataSetChanged()

                val llMax = LimitLine(data.yMax, String.format("%.4f", data.yMax))
                llMax.lineWidth = 1f
                llMax.lineColor = colorOrange
                // llMax.enableDashedLine(12f, 18f, 0f)
                llMax.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                llMax.textSize = 14f
                llMax.textColor = colorOrange
                tfFTitilliumWeb?.let {
                    llMax.typeface = it
                }

                val llMin = LimitLine(data.yMin, String.format("%.4f", data.yMin))
                llMin.lineWidth = 1f
                llMin.lineColor = colorGreen
                // llMin.enableDashedLine(12f, 18f, 0f)
                llMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
                llMin.textSize = 14f
                llMin.textColor = colorGreen
                tfFTitilliumWeb?.let {
                    llMin.typeface = it
                }

                axisLeft.removeAllLimitLines()

                val dataX = historyModel.chartHighlight?.let {
                    var x = -1f
                    entries.forEachIndexed { _, entry ->
                        val rate = entry.data as DateRate
                        if (rate.id == it.id) {
                            x = entry.x
                            return@forEachIndexed
                        }
                    }

                    if (x < 0) data.xMin else x
                } ?: data.xMax

                // if (historyModel.chartHighlightX < 0f) data.xMax else historyModel.chartHighlightX,
                highlightValue(dataX, 0, true)

                if (isResumed) {
                    handler.post {
                        axisLeft.addLimitLine(llMax)
                        axisLeft.addLimitLine(llMin)

                        binding.loading.hide()
                        binding.loadingContainer.visibility = View.GONE
                        visibility = View.VISIBLE
                        binding.chartInterval.visibility = View.VISIBLE

                        invalidate()
                        // animateX(300, Easing.Linear)
                    }
                } else {
                    axisLeft.addLimitLine(llMax)
                    axisLeft.addLimitLine(llMin)

                    binding.loading.hide()
                    binding.loadingContainer.visibility = View.GONE
                    visibility = View.VISIBLE
                    binding.chartInterval.visibility = View.VISIBLE

                    invalidate()
                }
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