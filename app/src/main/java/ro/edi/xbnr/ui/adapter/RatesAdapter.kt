package ro.edi.xbnr.ui.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
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
        return ratesModel.currencies.value?.size ?: 0
    }

    override fun getModel(): ViewModel {
        return ratesModel
    }

    override fun getItemLayoutId(position: Int): Int {
        return R.layout.currency_item
    }

    override fun onClick(position: Int) {

    }

    override fun onLongClick(position: Int): Boolean {
        return false
    }

    override fun bind(position: Int, binding: ViewDataBinding) {
        binding.root.findViewById<View>(R.id.currency_flag).setOnClickListener {
            ratesModel.getCurrency(position)?.let {
                ratesModel.setIsStarred(position, !it.isStarred)
            }
        }
    }
}