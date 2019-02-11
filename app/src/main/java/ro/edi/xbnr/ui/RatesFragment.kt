package ro.edi.xbnr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var ratesModel: RatesViewModel? = null

    companion object {
        fun newInstance(): RatesFragment {
            return RatesFragment()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // RatesFragmentBinding binding = DataBindingUtil.setContentView(this, R.layout.rates_fragment)
        // binding.setModel(ratesModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<RatesFragmentBinding>(
            inflater,
            R.layout.rates_fragment, container, false
        )

        ratesModel = ViewModelProviders.of(this).get(RatesViewModel::class.java)

        val adapter = RatesAdapter(ratesModel!!)
        adapter.setHasStableIds(true)

        ratesModel!!.getRates()
            .observe(this, Observer {
                binding.loading.hide()

                // FIXME replace with databinding
                if (it.currencies.isNullOrEmpty()) {
                    binding.empty.visibility = View.VISIBLE
                    binding.rates.visibility = View.GONE
                } else {
                    binding.empty.visibility = View.GONE
                    binding.rates.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            })

        // binding.model = ratesModel
        binding.rates.setHasFixedSize(true)
        binding.rates.adapter = adapter
        binding.rates.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        return binding.root
    }
}
