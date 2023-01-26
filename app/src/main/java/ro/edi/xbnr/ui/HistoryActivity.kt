/*
* Copyright 2019-2023 Eduard Scarlat
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.ActivityHistoryBinding
import ro.edi.xbnr.ui.viewmodel.CurrencyViewModel
import timber.log.Timber.Forest.i as logi

class HistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CURRENCY_ID = "ro.edi.xbnr.ui.history.extra_currency_id"
    }

    lateinit var binding: ActivityHistoryBinding

    private val currencyModel: CurrencyViewModel by viewModels { CurrencyViewModel.FACTORY }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currencyId = intent.getIntExtra(EXTRA_CURRENCY_ID, 0)

        currencyModel.currencyId = currencyId

        binding = DataBindingUtil.setContentView(this, R.layout.activity_history)

        binding.apply {
            lifecycleOwner = this@HistoryActivity
            model = currencyModel
        }

        initView(binding)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    HistoryFragment.newInstance(currencyId)
                )
                .commitNow()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)

        menu.findItem(R.id.action_star).isVisible = currencyModel.getIsStarred()?.not() ?: false
        menu.findItem(R.id.action_unstar).isVisible = currencyModel.getIsStarred() ?: false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_star -> currencyModel.setIsStarred(true)
            R.id.action_unstar -> currencyModel.setIsStarred(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView(binding: ActivityHistoryBinding) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currencyModel.currency.observe(this) { currency ->
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
        }
    }
}