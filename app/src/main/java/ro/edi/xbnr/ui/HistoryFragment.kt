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

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.tabs.TabLayout
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.model.DayRate
import ro.edi.xbnr.model.MonthRate
import ro.edi.xbnr.model.YearRate
import ro.edi.xbnr.ui.viewmodel.HistoryViewModel
import ro.edi.xbnr.ui.viewmodel.PREFS_KEY_CHART_INTERVAL
import java.math.RoundingMode
import java.text.NumberFormat
import timber.log.Timber.Forest.i as logi

class HistoryFragment : Fragment(), TabLayout.OnTabSelectedListener, OnChartValueSelectedListener {
    companion object {
        const val ARG_CURRENCY_ID = "ro.edi.xbnr.ui.history.arg_currency_id"

        fun newInstance(currencyId: Int) = HistoryFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CURRENCY_ID, currencyId)
            }
        }
    }

    private val nf = NumberFormat.getNumberInstance()

    private lateinit var historyModel: HistoryViewModel

    private var _binding: FragmentHistoryBinding? = null

    // this property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = historyModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(view.context)

        // TODO load this async?
        val tfTitilliumWeb = ResourcesCompat.getFont(view.context, R.font.titillium_web)

        val txtRonSymbol = getString(R.string.symbol_ron)
        val textColorTrendUp = view.context.getColor(R.color.textColorTrendUp)
        val textColorTrendDown = view.context.getColor(R.color.textColorTrendDown)

        val bkgChart = AppCompatResources.getDrawable(view.context, R.drawable.bkg_chart)
        val colorPrimary = view.context.getColor(getColorRes(view.context, R.attr.colorPrimary))

        nf.roundingMode = RoundingMode.HALF_UP
        nf.minimumFractionDigits = 4
        nf.maximumFractionDigits = 4

        // binding.chartLines.visibility = View.GONE
        // binding.chartCandlesticks.visibility = View.GONE
        // binding.loading.show()

        @Suppress("UNCHECKED_CAST")
        initChart(
            view,
            binding.chartLines as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
        )
        @Suppress("UNCHECKED_CAST")
        initChart(
            view,
            binding.chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
        )

        binding.tabs.apply {
            when (sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)) {
                1 -> selectTab(getTabAt(0))
                3, 6 -> selectTab(getTabAt(1))
                12 -> selectTab(getTabAt(2))
                60 -> selectTab(getTabAt(3))
                -1 -> selectTab(getTabAt(4))
                else -> selectTab(getTabAt(0))
            }

            clearOnTabSelectedListeners()
            addOnTabSelectedListener(this@HistoryFragment)
        }

        historyModel.dayRates.observe(viewLifecycleOwner, Observer { rates ->
            logi("historyModel day rates changed")

            val monthsCount = sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)
            if (monthsCount == 60 || monthsCount == -1) {
                // do nothing if 5Y or MAX selected
                return@Observer
            }

            binding.chartLines.visibility = View.GONE
            binding.chartCandlesticks.visibility = View.GONE
            binding.loading.show()

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

            @Suppress("UNCHECKED_CAST")
            updateChart(
                binding.chartLines as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                LineData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                entries,
                tfTitilliumWeb, txtRonSymbol, textColorTrendUp, textColorTrendDown
            )
        })

        historyModel.monthRates.observe(viewLifecycleOwner, Observer { rates ->
            logi("historyModel month rates changed")

            val monthsCount = sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)
            if (monthsCount != 60) {
                // do nothing if 5Y not selected
                return@Observer
            }

            binding.chartLines.visibility = View.GONE
            binding.chartCandlesticks.visibility = View.GONE
            binding.loading.show()

            val entries = mutableListOf<CandleEntry>()
            rates.forEachIndexed { index, rate ->
                val entry = CandleEntry(
                    index.toFloat(),
                    rate.max.toFloat(),
                    rate.min.toFloat(),
                    rate.min.toFloat(), // open
                    rate.max.toFloat(), // close
                    rate
                )
                entries.add(entry)
            }

            val dataSet = CandleDataSet(entries, "rates").apply {
                setDrawValues(false)
                setDrawHighlightIndicators(false)

                axisDependency = YAxis.AxisDependency.LEFT

                shadowColor = colorPrimary
                shadowWidth = 2.0f
                decreasingColor = colorPrimary
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = colorPrimary
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = colorPrimary
            }

            @Suppress("UNCHECKED_CAST")
            updateChart(
                binding.chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                CandleData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                entries as MutableList<Entry>,
                tfTitilliumWeb, txtRonSymbol, textColorTrendUp, textColorTrendDown
            )
        })

        historyModel.yearRates.observe(viewLifecycleOwner, Observer { rates ->
            logi("historyModel year rates changed")

            val monthsCount = sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)
            if (monthsCount != -1) {
                // do nothing if not MAX selected
                return@Observer
            }

            binding.chartLines.visibility = View.GONE
            binding.chartCandlesticks.visibility = View.GONE
            binding.loading.show()

            val entries = mutableListOf<CandleEntry>()
            rates.forEachIndexed { index, rate ->
                val entry = CandleEntry(
                    index.toFloat(),
                    rate.max.toFloat(),
                    rate.min.toFloat(),
                    rate.min.toFloat(), // open
                    rate.max.toFloat(), // close
                    rate
                )
                entries.add(entry)
            }

            val dataSet = CandleDataSet(entries, "rates").apply {
                setDrawValues(false)
                setDrawHighlightIndicators(false)

                axisDependency = YAxis.AxisDependency.LEFT

                shadowColor = colorPrimary
                shadowWidth = 2.0f
                decreasingColor = colorPrimary
                decreasingPaintStyle = Paint.Style.FILL
                increasingColor = colorPrimary
                increasingPaintStyle = Paint.Style.FILL
                neutralColor = colorPrimary
            }

            @Suppress("UNCHECKED_CAST")
            updateChart(
                binding.chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                CandleData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                entries as MutableList<Entry>,
                tfTitilliumWeb, txtRonSymbol, textColorTrendUp, textColorTrendDown
            )
        })
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

    private fun initChart(
        view: View,
        chart: BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
    ) {
        val tfFiraCondensed = ResourcesCompat.getFont(view.context, R.font.fira_sans_condensed)

        val textColorSecondary = view.context.getColor(
            getColorRes(view.context, android.R.attr.textColorSecondary)
        )

        chart.apply {
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
            marker.setOffset(-10f, -10f) // FIXME drawable size * dpi / 2?
            setMarker(marker)

            setNoDataText(getString(R.string.no_data_found))
            setNoDataTextColor(textColorSecondary)
            tfFiraCondensed?.let {
                setNoDataTextTypeface(it)
            }

            setOnChartValueSelectedListener(this@HistoryFragment)
        }
    }

    private fun updateChart(
        chart: BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
        chartData: BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
        entries: MutableList<Entry>,
        tfTitilliumWeb: Typeface?,
        txtRonSymbol: String,
        textColorTrendUp: Int,
        textColorTrendDown: Int
    ) {
        chart.apply {
            data = chartData
            notifyDataSetChanged()

            val llMax = LimitLine(data.yMax, txtRonSymbol.plus(nf.format(data.yMax)))
            llMax.lineWidth = 1f
            llMax.lineColor = textColorTrendUp
            // llMax.enableDashedLine(12f, 18f, 0f)
            llMax.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            llMax.textSize = 16f
            llMax.textColor = textColorTrendUp
            tfTitilliumWeb?.let {
                llMax.typeface = it
            }

            val llMin = LimitLine(data.yMin, txtRonSymbol.plus(nf.format(data.yMin)))
            llMin.lineWidth = 1f
            llMin.lineColor = textColorTrendDown
            // llMin.enableDashedLine(12f, 18f, 0f)
            llMin.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM
            llMin.textSize = 16f
            llMin.textColor = textColorTrendDown
            tfTitilliumWeb?.let {
                llMin.typeface = it
            }

            axisLeft.removeAllLimitLines()

            val dataX = historyModel.highlightedEntry?.let {
                var x = -1f
                entries.forEachIndexed { _, entry ->
                    val hRate = it.data
                    when (val rate = entry.data) {
                        is DayRate -> {
                            if (hRate is DayRate && rate.id == hRate.id) {
                                x = entry.x
                                return@forEachIndexed
                            }
                        }
                        is MonthRate -> {
                            if (hRate is MonthRate && rate.id == hRate.id) {
                                x = entry.x
                                return@forEachIndexed
                            }
                        }
                        is YearRate -> {
                            if (hRate is YearRate && rate.id == hRate.id) {
                                x = entry.x
                                return@forEachIndexed
                            }
                        }
                    }
                }

                if (x < 0) data.xMax else x
            } ?: data.xMax

            highlightValue(dataX, 0, true)

            if (isResumed) {
                if (handler != null) {
                    handler.post {
                        axisLeft.addLimitLine(llMax)
                        axisLeft.addLimitLine(llMin)

                        binding.loading.hide()
                        chart.visibility = View.VISIBLE
                        chart.invalidate()
                        // animateX(300, Easing.Linear)
                    }
                }
            } else {
                axisLeft.addLimitLine(llMax)
                axisLeft.addLimitLine(llMin)

                binding.loading.hide()
                chart.visibility = View.VISIBLE
                chart.invalidate()
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val interval = when (binding.tabs.selectedTabPosition) {
            0 -> 1
            1 -> 6
            2 -> 12
            3 -> 60
            4 -> -1
            else -> 1
        }

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(tab.view.context)
        sharedPrefs.edit()
            .putInt(PREFS_KEY_CHART_INTERVAL, interval)
            .apply()
    }

    override fun onTabReselected(tab: TabLayout.Tab) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab) {

    }

    override fun onValueSelected(e: Entry, h: Highlight) {
        // logi("@qqq: onValueSelected: $e")
        historyModel.highlightedEntry = e
        show(e)
    }

    override fun onNothingSelected() {
        historyModel.highlightedEntry = null
        show(null)
    }

    private fun show(entry: Entry?) {
        activity ?: return

        (activity as HistoryActivity).run {
            when (val rate = entry?.data) {
                is DayRate -> {
                    binding.currencyDate.visibility = View.VISIBLE
                    binding.currencyDate.text =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            historyModel.getDisplayDate(rate.date).replaceFirst(' ', '\n')
                        } else {
                            historyModel.getDisplayDate(rate.date)
                        }

                    binding.currencyRonMax.visibility = View.GONE
                    binding.currencyRateMax.visibility = View.GONE
                    binding.currencyRonMin.visibility = View.GONE
                    binding.currencyRateMin.visibility = View.GONE

                    binding.currencyRon.visibility = View.VISIBLE
                    binding.currencyRate.visibility = View.VISIBLE
                    binding.currencyRate.text = historyModel.getDisplayRate(rate.rate)

                    val trend = historyModel.getDisplayTrend(rate)
                    if (trend.isEmpty()) {
                        binding.currencyTrend.visibility = View.GONE
                    } else {
                        binding.currencyTrend.visibility = View.VISIBLE
                        binding.currencyTrend.text = trend
                        when {
                            trend.startsWith('+') -> {
                                val textColorTrendUp = getColor(R.color.textColorTrendUp)
                                binding.currencyTrend.setTextColor(textColorTrendUp)
                            }
                            trend.startsWith('-') -> {
                                val textColorTrendDown = getColor(R.color.textColorTrendDown)
                                binding.currencyTrend.setTextColor(textColorTrendDown)
                            }
                            else -> {
                                val textColorPrimary =
                                    getColor(getColorRes(this, android.R.attr.textColorPrimary))
                                binding.currencyTrend.setTextColor(textColorPrimary)
                            }
                        }
                    }
                }
                is MonthRate -> {
                    binding.currencyDate.visibility = View.VISIBLE
                    binding.currencyDate.text =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            historyModel.getDisplayMonth(rate.month).replaceFirst(' ', '\n')
                        } else {
                            historyModel.getDisplayMonth(rate.month)
                        }

                    binding.currencyRon.visibility = View.GONE
                    binding.currencyRate.visibility = View.GONE
                    binding.currencyTrend.visibility = View.GONE

                    binding.currencyRonMax.visibility = View.VISIBLE
                    binding.currencyRateMax.visibility = View.VISIBLE
                    binding.currencyRonMin.visibility = View.VISIBLE
                    binding.currencyRateMin.visibility = View.VISIBLE

                    binding.currencyRateMax.text = historyModel.getDisplayRate(rate.max)
                    binding.currencyRateMin.text = historyModel.getDisplayRate(rate.min)
                }
                is YearRate -> {
                    binding.currencyDate.visibility = View.VISIBLE
                    binding.currencyDate.text = rate.year

                    binding.currencyRon.visibility = View.GONE
                    binding.currencyRate.visibility = View.GONE
                    binding.currencyTrend.visibility = View.GONE

                    binding.currencyRonMax.visibility = View.VISIBLE
                    binding.currencyRateMax.visibility = View.VISIBLE
                    binding.currencyRonMin.visibility = View.VISIBLE
                    binding.currencyRateMin.visibility = View.VISIBLE

                    binding.currencyRateMax.text = historyModel.getDisplayRate(rate.max)
                    binding.currencyRateMin.text = historyModel.getDisplayRate(rate.min)
                }
                else -> {
                    binding.currencyDate.visibility = View.INVISIBLE

                    binding.currencyRon.visibility = View.GONE
                    binding.currencyRate.visibility = View.GONE
                    binding.currencyTrend.visibility = View.GONE

                    binding.currencyRonMax.visibility = View.GONE
                    binding.currencyRateMax.visibility = View.GONE
                    binding.currencyRonMin.visibility = View.GONE
                    binding.currencyRateMin.visibility = View.GONE
                }
            }
        }
    }

    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                (activity as AppCompatActivity).application,
                arguments?.getInt(ARG_CURRENCY_ID, 0) ?: 0
            ) as T
        }
    }
}