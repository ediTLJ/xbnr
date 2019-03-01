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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.ActivityHistoryBinding
import ro.edi.xbnr.ui.viewmodel.CurrencyViewModel
import timber.log.Timber.i as logi

class HistoryActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CURRENCY_ID = "ro.edi.xbnr.ui.history.extra_currency_id"
    }

    private val currencyModel: CurrencyViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this, factory).get(CurrencyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityHistoryBinding = DataBindingUtil.setContentView(this, R.layout.activity_history)
        binding.model = currencyModel

        currencyModel.currency.observe(this, Observer {
            logi("found currency: %s", it)
            invalidateOptionsMenu()
            binding.invalidateAll()
        })

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    HistoryFragment.newInstance(intent.getIntExtra(EXTRA_CURRENCY_ID, -1))
                )
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)

        menu?.let {
            it.findItem(R.id.action_star).isVisible = currencyModel.getIsStarred()?.not() ?: false
            it.findItem(R.id.action_unstar).isVisible = currencyModel.getIsStarred() ?: false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_star -> currencyModel.setIsStarred(true)
            R.id.action_unstar -> currencyModel.setIsStarred(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CurrencyViewModel(
                application,
                intent.getIntExtra(EXTRA_CURRENCY_ID, -1)
            ) as T
        }
    }
}