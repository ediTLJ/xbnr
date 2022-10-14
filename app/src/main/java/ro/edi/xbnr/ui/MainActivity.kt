/*
* Copyright 2019-2021 Eduard Scarlat
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
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import ro.edi.xbnr.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightMode == Configuration.UI_MODE_NIGHT_NO) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }

        setContentView(R.layout.activity_main)
        initView()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RatesFragment.newInstance())
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> InfoDialogFragment().show(supportFragmentManager, "dialog_info")
            R.id.action_settings -> {
                val iSettings = Intent(this, SettingsActivity::class.java)
                iSettings.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(iSettings)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _, insets ->
            toolbar.setMargins(
                insets.systemWindowInsetTop,
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetRight
            )
            insets
        }
    }

    private fun View.setMargins(topValue: Int, leftValue: Int, rightValue: Int) =
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = topValue
            leftMargin = leftValue
            rightMargin = rightValue
        }
}