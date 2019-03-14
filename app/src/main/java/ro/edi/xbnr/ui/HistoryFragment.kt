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
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import ro.edi.util.getColorRes
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentHistoryBinding
import ro.edi.xbnr.model.DateRate
import ro.edi.xbnr.ui.viewmodel.HistoryViewModel
import timber.log.Timber.d as logd
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

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val colorAccent = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, R.attr.colorAccent)
        )
        val textColorSecondary = ContextCompat.getColor(
            binding.root.context,
            getColorRes(binding.root.context, android.R.attr.textColorSecondary)
        )
        val bkgChart = ContextCompat.getDrawable(binding.root.context, R.drawable.bkg_chart)

        binding.lineChart.apply {
            isAutoScaleMinMaxEnabled = true
            isKeepPositionOnRotation = true
            legend.isEnabled = false
            description.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            setNoDataText(getString(R.string.no_data_found))
            setNoDataTextColor(textColorSecondary)
            ResourcesCompat.getFont(context, R.font.fira_sans_condensed_medium)?.let {
                setNoDataTextTypeface(it)
            }

            val clickListener = object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    historyModel.chartHighlightX = h.x
                    show(e.data as DateRate)
                }

                override fun onNothingSelected() {
                    historyModel.chartHighlightX = -1f
                    historyModel.rates.value?.lastOrNull()?.let {
                        show(it)
                    }
                }

                private fun show(rate: DateRate) {
                    activity?.run {
                        findViewById<TextView>(R.id.currency_date).text =
                            LocalDate.parse(rate.date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

                        findViewById<TextView>(R.id.currency_value).text = String.format("%.4f", rate.rate)
                    }
                }
            }
            setOnChartValueSelectedListener(clickListener)

            // setVisibleXRangeMaximum(20f)
            minOffset = 8f

            val marker = MarkerImage(context, R.drawable.ic_dot)
            marker.setOffset(-12f, -12f) // drawable size + line width
            setMarker(marker)
        }

        binding.chartMode.apply {
            val checkedId = when (sharedPrefs.getString("chart_line_mode", LineDataSet.Mode.HORIZONTAL_BEZIER.name)) {
                LineDataSet.Mode.HORIZONTAL_BEZIER.name -> R.id.mode_bezier
                LineDataSet.Mode.STEPPED.name -> R.id.mode_stepped
                LineDataSet.Mode.LINEAR.name -> R.id.mode_linear
                else -> R.id.mode_bezier
            }

            for (i in 0 until childCount) {
                val chip = getChildAt(i) as Chip
                chip.isClickable = chip.id != checkedId
                chip.isChecked = chip.id == checkedId
                // let's keep them both visible for now... it's an interesting effect
                // chip.isChipIconVisible = chip.id != chipGroup.checkedChipId
                // chip.isCheckedIconVisible = chip.id == chipGroup.checkedChipId
            }
        }

        binding.chartMode.setOnCheckedChangeListener { chipGroup, id ->
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.isClickable = chip.id != chipGroup.checkedChipId
                chip.isChecked = chip.id == chipGroup.checkedChipId
                // let's keep them both visible for now... it's an interesting effect
                // chip.isChipIconVisible = chip.id != chipGroup.checkedChipId
                // chip.isCheckedIconVisible = chip.id == chipGroup.checkedChipId
            }

            binding.lineChart.apply {
                if (data == null || data.dataSetCount == 0) {
                    return@setOnCheckedChangeListener
                }

                val dataSet = data.getDataSetByIndex(0) as LineDataSet
                dataSet.apply {
                    when (id) {
                        R.id.mode_bezier -> mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        R.id.mode_stepped -> mode = LineDataSet.Mode.STEPPED
                        R.id.mode_linear -> mode = LineDataSet.Mode.LINEAR
                    }

                    sharedPrefs.edit()
                        .putString("chart_line_mode", mode.name)
                        .apply()
                }

                animateX(300, Easing.Linear)
            }
        }

        historyModel.rates.observe(viewLifecycleOwner, Observer { rates ->
            logi("ratesModel currencies changed")

            if (rates.isNullOrEmpty()) {
                binding.loading.show()
                binding.lineChart.visibility = View.GONE
            } else {
                binding.loading.hide()

                val entries = mutableListOf<Entry>()

                rates.forEachIndexed { index, rate ->
                    entries.add(Entry(index.toFloat(), rate.rate.toFloat(), rate))
                }

                val dataSet = LineDataSet(entries, "rates").apply {
                    axisDependency = YAxis.AxisDependency.LEFT

                    mode = LineDataSet.Mode.valueOf(
                        sharedPrefs.getString("chart_line_mode", LineDataSet.Mode.HORIZONTAL_BEZIER.name)
                            ?: LineDataSet.Mode.HORIZONTAL_BEZIER.name
                    )

                    lineWidth = 2.0f
                    setDrawCircles(false)

                    setDrawFilled(true)
                    fillDrawable = bkgChart

                    setDrawHighlightIndicators(false)

                    setDrawValues(false)
                    color = colorAccent
                }

                binding.lineChart.apply {
                    val wasEmpty = data == null || data.dataSetCount == 0
                        || data.getDataSetByIndex(0).entryCount == 0

                    data = LineData(dataSet)
                    notifyDataSetChanged()

                    highlightValue(
                        if (historyModel.chartHighlightX < 0f) data.xMax else historyModel.chartHighlightX,
                        0, true
                    )

                    visibility = View.VISIBLE

                    if (wasEmpty) {
                        animateX(300, Easing.Linear)
                    } else {
                        invalidate()
                    }
                }

                binding.chartMode.visibility = View.VISIBLE
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