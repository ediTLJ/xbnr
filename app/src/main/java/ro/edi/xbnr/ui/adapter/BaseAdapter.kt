package ro.edi.xbnr.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import ro.edi.xbnr.BR

abstract class BaseAdapter() :
    RecyclerView.Adapter<BaseAdapter.BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getModel(), position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemLayoutId(position)
    }

    protected abstract fun getModel(): ViewModel

    protected abstract fun getItemLayoutId(position: Int): Int

    inner class BaseViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ViewModel, position: Int) {
            binding.root.setOnClickListener { }
            binding.setVariable(BR.model, model)
            binding.setVariable(BR.position, position)
            binding.executePendingBindings()
        }
    }
}
