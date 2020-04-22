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

import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.fragment_history.*
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.model.DayRate
import ro.edi.xbnr.ui.viewmodel.HistoryViewModel
import ro.edi.xbnr.ui.viewmodel.PREFS_KEY_CHART_INTERVAL
import java.math.RoundingMode
import java.text.NumberFormat
import kotlinx.android.synthetic.main.activity_history.currency_date as tvDate
import kotlinx.android.synthetic.main.activity_history.currency_trend as tvTrend
import kotlinx.android.synthetic.main.activity_history.currency_value as tvRate
import kotlinx.android.synthetic.main.fragment_history.chart_candlesticks as chartCandlesticks
import kotlinx.android.synthetic.main.fragment_history.chart_lines as chartLines
import timber.log.Timber.i as logi

class HistoryFragment : Fragment(), TabLayout.OnTabSelectedListener, OnChartValueSelectedListener {
    companion object {
        const val ARG_CURRENCY_ID = "ro.edi.xbnr.ui.history.arg_currency_id"

        fun newInstance(currencyId: Int) = HistoryFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CURRENCY_ID, currencyId)
            }
        }
    }

    private lateinit var historyModel: HistoryViewModel

    private val nf = NumberFormat.getNumberInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            model = historyModel
        }

        return binding.root
    }

    private fun initChart(
        view: View,
        chart: BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
    ) {
        val tfFiraCondensed = ResourcesCompat.getFont(view.context, R.font.fira_sans_condensed)

        val textColorSecondary = ContextCompat.getColor(
            view.context,
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
            marker.setOffset(-10f, -10f) // drawable size + line width
            setMarker(marker)

            setNoDataText(getString(R.string.no_data_found))
            setNoDataTextColor(textColorSecondary)
            tfFiraCondensed?.let {
                setNoDataTextTypeface(it)
            }

            setOnChartValueSelectedListener(this@HistoryFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        val colorPrimary = ContextCompat.getColor(
            view.context,
            getColorRes(view.context, R.attr.colorPrimary)
        )

        nf.roundingMode = RoundingMode.HALF_UP
        nf.minimumFractionDigits = 4
        nf.maximumFractionDigits = 4

        @Suppress("UNCHECKED_CAST")
        initChart(
            view,
            chartLines as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
        )
        @Suppress("UNCHECKED_CAST")
        initChart(
            view,
            chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>
        )

        tabs.apply {
            when (sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)) {
                1 -> selectTab(getTabAt(0))
                3, 6 -> selectTab(getTabAt(1))
                12 -> selectTab(getTabAt(2))
                60 -> selectTab(getTabAt(3))
                0 -> selectTab(getTabAt(4))
                else -> selectTab(getTabAt(0))
            }

            clearOnTabSelectedListeners()
            addOnTabSelectedListener(this@HistoryFragment)
        }

        historyModel.rates.observe(viewLifecycleOwner, Observer { rates ->
            logi("historyModel rates changed")

            chartLines.visibility = View.GONE
            chartCandlesticks.visibility = View.GONE
            loading.show()

            when (sharedPrefs.getInt(PREFS_KEY_CHART_INTERVAL, 1)) {
                1, 3, 6, 12 -> {
                    val bkgChart = ContextCompat.getDrawable(view.context, R.drawable.bkg_chart)

                    val entries = mutableListOf<Entry>()
                    rates.forEachIndexed { index, dayRate ->
                        entries.add(Entry(index.toFloat(), dayRate.rate.toFloat(), dayRate))
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
                        view,
                        chartLines as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                        LineData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                        entries
                    )
                }
                60 -> { // 5 years
                    val entries = mutableListOf<CandleEntry>()
                    var idx = 0F

                    val bars = rates.groupBy { it.date.subSequence(0, 7) }
                    bars.forEach { (_, monthRates) ->
                        val bar = CandleEntry(
                            idx++,
                            monthRates.maxBy { it.rate }?.rate?.toFloat() ?: 0F,
                            monthRates.minBy { it.rate }?.rate?.toFloat() ?: 0F,
                            monthRates.first().rate.toFloat(),
                            monthRates.last().rate.toFloat(),
                            monthRates.last()
                        )
                        entries.add(bar)
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
                        view,
                        chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                        CandleData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                        entries as MutableList<Entry>
                    )
                }
                0 -> { // MAX
                    val entries = mutableListOf<CandleEntry>()
                    var idx = 0F

                    val bars = rates.groupBy { it.date.subSequence(0, 4) }
                    bars.forEach { (_, yearRates) ->
                        val bar = CandleEntry(
                            idx++,
                            yearRates.maxBy { it.rate }?.rate?.toFloat() ?: 0F,
                            yearRates.minBy { it.rate }?.rate?.toFloat() ?: 0F,
                            yearRates.first().rate.toFloat(),
                            yearRates.last().rate.toFloat(),
                            yearRates.last()
                        )
                        entries.add(bar)
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
                        view,
                        chartCandlesticks as BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
                        CandleData(dataSet) as BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
                        entries as MutableList<Entry>
                    )
                }
                else -> {
                    // updateChart(view, chartLines, entries)
                }
            }
        })
    }

    private fun updateChart(
        view: View,
        chart: BarLineChartBase<BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>>,
        chartData: BarLineScatterCandleBubbleData<ILineScatterCandleRadarDataSet<Entry>>,
        entries: MutableList<Entry>
    ) {
        // TODO load these async?
        ////
        val tfTitilliumWeb = ResourcesCompat.getFont(view.context, R.font.titillium_web)

        val textColorTrendUp = ContextCompat.getColor(view.context, R.color.textColorTrendUp)
        val textColorTrendDown = ContextCompat.getColor(view.context, R.color.textColorTrendDown)

        val txtRonSymbol = getString(R.string.symbol_ron)
        ////

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

            val dataX = historyModel.chartHighlight?.let {
                var x = -1f
                entries.forEachIndexed { _, entry ->
                    val rate = entry.data as DayRate
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
                if (handler != null) {
                    handler.post {
                        axisLeft.addLimitLine(llMax)
                        axisLeft.addLimitLine(llMin)

                        loading.hide()
                        chart.visibility = View.VISIBLE
                        chart.invalidate()
                        // animateX(300, Easing.Linear)
                    }
                }
            } else {
                axisLeft.addLimitLine(llMax)
                axisLeft.addLimitLine(llMin)

                loading.hide()
                chart.visibility = View.VISIBLE
                chart.invalidate()
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val interval = when (tabs.selectedTabPosition) {
            0 -> 1
            1 -> 6
            2 -> 12
            3 -> 60
            4 -> 0
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
        val rate = e.data as DayRate
        historyModel.chartHighlight = rate
        show(rate)
    }

    override fun onNothingSelected() {
        historyModel.chartHighlight = null
        historyModel.rates.value?.lastOrNull()?.let {
            show(it)
        }
    }

    // FIXME show something else when candlesticks chart selected
    private fun show(rate: DayRate) {
        activity?.run {
            tvDate.text =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    historyModel.getDisplayDate(rate).replaceFirst(' ', '\n')
                } else {
                    // the date textview in portrait mode is actually in the fragment
                    historyModel.getDisplayDate(rate)
                }
            tvRate.text = historyModel.getDisplayRate(rate)

            val trend = historyModel.getDisplayTrend(rate)
            if (trend.isEmpty()) {
                tvTrend.visibility = View.GONE
            } else {
                tvTrend.visibility = View.VISIBLE
                tvTrend.text = trend
                when {
                    trend.startsWith('+') -> {
                        val textColorTrendUp =
                            ContextCompat.getColor(this, R.color.textColorTrendUp)
                        tvTrend.setTextColor(textColorTrendUp)
                    }
                    trend.startsWith('-') -> {
                        val textColorTrendDown =
                            ContextCompat.getColor(this, R.color.textColorTrendDown)
                        tvTrend.setTextColor(textColorTrendDown)
                    }
                    else -> {
                        val textColorPrimary = ContextCompat.getColor(
                            this,
                            getColorRes(this, android.R.attr.textColorPrimary)
                        )
                        tvTrend.setTextColor(textColorPrimary)
                    }
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