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

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import ro.edi.xbnr.R
import ro.edi.xbnr.ui.HistoryActivity
import ro.edi.xbnr.ui.viewmodel.RatesViewModel

class RatesAdapter(private val ratesModel: RatesViewModel) :
    BaseAdapter() {

    override fun getItemId(position: Int): Long {
        return ratesModel.getCurrencyId(position).toLong()
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

    override fun onClick(context: Context, position: Int) {
        val i = Intent(context, HistoryActivity::class.java)
        i.putExtra(HistoryActivity.EXTRA_CURRENCY_ID, ratesModel.getCurrencyId(position))
        context.startActivity(i)
    }

    override fun onLongClick(context: Context, position: Int): Boolean {
        return false
    }

    override fun bind(position: Int, binding: ViewDataBinding) {
        val vFlag = binding.root.findViewById<View>(R.id.currency_flag)
        // val vFlagChecked = binding.root.findViewById<View>(R.id.currency_flag_checked)

        vFlag.setOnClickListener {
            // binding.root.isActivated = true
            // it.visibility = View.GONE
            // vFlagChecked.visibility = View.VISIBLE
            ratesModel.getCurrency(position)?.let {
                ratesModel.setIsStarred(position, !it.isStarred)
            }
        }

//        vFlagChecked.setOnClickListener {
//            // binding.root.isActivated = false
//            // it.visibility = View.GONE
//            // vFlag.visibility = View.VISIBLE
//            ratesModel.getCurrency(position)?.let {
//                ratesModel.setIsStarred(position, !it.isStarred)
//            }
//        }
    }
}