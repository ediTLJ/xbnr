package ro.edi.xbnr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
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
            binding.loading.hide()

            // FIXME replace with databinding
            if (it.isNullOrEmpty()) {
                binding.empty.visibility = View.VISIBLE
                binding.rates.visibility = View.GONE
            } else {
                binding.empty.visibility = View.GONE
                binding.rates.visibility = View.VISIBLE

                ratesAdapter.notifyDataSetChanged()

                val latestDate = ratesModel.getCurrency(0)?.date

                val toolbar = (activity as MainActivity).supportActionBar

                // FIXME different color when new date
//                    if (toolbar?.subtitle == latestDate) {
//                        toolbar?.setSubtitleTextColor(
//                            ContextCompat.getColor(
//                                activity as MainActivity,
//                                getColorRes(
//                                    activity as MainActivity,
//                                    android.R.attr.textColorSecondary
//                                )
//                            )
//                        )
//                    } else {
                toolbar?.subtitle = latestDate
//                        toolbar?.setSubtitleTextColor(
//                            ContextCompat.getColor(
//                                activity as MainActivity,
//                                getColorRes(activity as MainActivity, android.R.attr.colorPrimary)
//                            )
//                        )
//                    }
            }
        })

        ratesModel.previousRates.observe(this, Observer {
            ratesAdapter.notifyDataSetChanged()
        })

        with(binding.rates) {
            setHasFixedSize(true)
            adapter = ratesAdapter

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(context, R.drawable.divider)?.let { divider.setDrawable(it) }
            addItemDecoration(divider)
        }

        return binding.root
    }
}
