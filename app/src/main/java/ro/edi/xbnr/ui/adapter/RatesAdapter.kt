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
package ro.edi.xbnr.ui.adapter

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.CurrencyItemBinding
import ro.edi.xbnr.model.Currency
import ro.edi.xbnr.ui.HistoryActivity
import ro.edi.xbnr.ui.viewmodel.RatesViewModel

class RatesAdapter(private val ratesModel: RatesViewModel) : BaseAdapter<Currency>(CurrencyDiffCallback()) {
    companion object {
        const val CURRENCY_RATE = "currency_rate"
        const val CURRENCY_DATE = "currency_date"
    }

    override fun getModel(): ViewModel {
        return ratesModel
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun getItemLayoutId(position: Int): Int {
        return R.layout.currency_item
    }

    override fun onItemClick(itemView: View, position: Int) {
        val i = Intent(itemView.context, HistoryActivity::class.java)
        i.putExtra(HistoryActivity.EXTRA_CURRENCY_ID, getItem(position).id)
        itemView.context.startActivity(i)
    }

    override fun getClickableViewIds(): IntArray? {
        val ids = IntArray(1)
        ids[0] = R.id.currency_flag

        return ids
    }

    override fun onClick(v: View, position: Int) {
        if (v.id == R.id.currency_flag) {
            ratesModel.setIsStarred(position, !getItem(position).isStarred)
        }
    }

    override fun bind(binding: ViewDataBinding, position: Int, payloads: MutableList<Any>) {
        val b = binding as CurrencyItemBinding

        val payload = payloads.first() as Set<*>
        payload.forEach {
            when (it) {
                CURRENCY_RATE -> b.currencyValue.text = String.format("%.4f", getItem(position).rate)
                CURRENCY_DATE -> b.currencyValue.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        ratesModel.getTrendColorRes(position)
                    )
                )
            }
        }
    }

    class CurrencyDiffCallback : DiffUtil.ItemCallback<Currency>() {
        override fun areItemsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Currency, newItem: Currency): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Currency, newItem: Currency): Any? {
            val payload = mutableSetOf<String>()

            if (oldItem.rate != newItem.rate) {
                payload.add(CURRENCY_RATE)
            }
            if (oldItem.date != newItem.date) {
                payload.add(CURRENCY_DATE)
            }

            if (payload.isEmpty()) {
                return null
            }

            return payload
        }
    }
}