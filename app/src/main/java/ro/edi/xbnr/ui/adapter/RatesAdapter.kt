package ro.edi.xbnr.ui.adapter

import androidx.lifecycle.ViewModel
import ro.edi.xbnr.R
import ro.edi.xbnr.ui.viewmodel.RatesViewModel

class RatesAdapter(private val ratesModel: RatesViewModel) :
    BaseAdapter() {

    override fun getItemId(position: Int): Long {
        return ratesModel.getCurrency(position).hashCode().toLong()
    }

    override fun getItemCount(): Int {
        ratesModel.getRates().value?.currencies?.let {
            return it.size
        }

        return 0
    }

    override fun getModel(): ViewModel {
        return ratesModel
    }

    override fun getItemLayoutId(position: Int): Int {
        return R.layout.currency_item
    }
}