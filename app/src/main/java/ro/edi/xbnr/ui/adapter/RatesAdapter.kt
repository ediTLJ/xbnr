package ro.edi.xbnr.ui.adapter

import androidx.lifecycle.ViewModel
import ro.edi.xbnr.R
import ro.edi.xbnr.ui.viewmodel.RatesViewModel

class RatesAdapter(private val ratesModel: RatesViewModel) :
    BaseAdapter() {

    override fun getItemId(position: Int): Long {
        ratesModel.getCurrency(position)?.let {
            return it.id.toLong()
        }

        return 0
    }

    override fun getItemCount(): Int {
        return ratesModel.getCurrencies().value?.size ?: 0
    }

    override fun getModel(): ViewModel {
        return ratesModel
    }

    override fun getItemLayoutId(position: Int): Int {
        return R.layout.currency_item
    }
}