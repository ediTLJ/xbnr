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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.ActivityHistoryBinding
import ro.edi.xbnr.ui.viewmodel.CurrencyViewModel
import timber.log.Timber.i as logi

class HistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CURRENCY_ID = "ro.edi.xbnr.ui.history.extra_currency_id"
    }

    private val currencyModel: CurrencyViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, factory).get(CurrencyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityHistoryBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_history)
        binding.lifecycleOwner = this
        binding.model = currencyModel

        initView(binding)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    HistoryFragment.newInstance(intent.getIntExtra(EXTRA_CURRENCY_ID, 0))
                )
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)

        menu.findItem(R.id.action_star).isVisible = currencyModel.getIsStarred()?.not() ?: false
        menu.findItem(R.id.action_unstar).isVisible = currencyModel.getIsStarred() ?: false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_star -> currencyModel.setIsStarred(true)
            R.id.action_unstar -> currencyModel.setIsStarred(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView(binding: ActivityHistoryBinding) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currencyModel.currency.observe(this, Observer { currency ->
            logi("found currency: %s", currency)
            invalidateOptionsMenu()
            binding.invalidateAll()

            binding.fabConverter.setOnClickListener {
                if (currency == null) {
                    // just in case...
                    return@setOnClickListener
                }

                // TODO open converter screen using historyModel.currencyId & historyModel.chartHighlight data?
                val i = Intent(this, ConverterActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                i.putExtra(ConverterActivity.EXTRA_CURRENCY_ID, currency.id)
                i.putExtra(ConverterActivity.EXTRA_CURRENCY_DATE, currency.date)
                startActivity(i)
            }
        })
    }

    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(
                application,
                intent.getIntExtra(EXTRA_CURRENCY_ID, 0)
            ) as T
        }
    }
}