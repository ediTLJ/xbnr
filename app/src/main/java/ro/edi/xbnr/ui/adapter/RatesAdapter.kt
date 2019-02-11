package ro.edi.xbnr.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ro.edi.xbnr.BR
import ro.edi.xbnr.databinding.CurrencyItemBinding
import ro.edi.xbnr.ui.rates.RatesViewModel

class RatesAdapter(private val ratesModel: RatesViewModel) :
    RecyclerView.Adapter<RatesAdapter.RatesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CurrencyItemBinding.inflate(layoutInflater, parent, false)
        return RatesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatesViewHolder, position: Int) {
        holder.bind(ratesModel, position)
    }

    override fun getItemId(position: Int): Long {
        return ratesModel.getCurrency(position).hashCode().toLong()

        // ratesModel.getRates().value?.currencies?.let {
        //     return it[position].code.hashCode().toLong()
        // }
        // return -1
    }

    override fun getItemCount(): Int {
        ratesModel.getRates().value?.currencies?.let {
            return it.size
        }

        return 0
    }

    inner class RatesViewHolder(private val binding: CurrencyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ratesModel: RatesViewModel, position: Int) {
            binding.root.setOnClickListener { }
            binding.setVariable(BR.model, ratesModel)
            binding.setVariable(BR.position, position)
            binding.executePendingBindings()
        }
    }
}