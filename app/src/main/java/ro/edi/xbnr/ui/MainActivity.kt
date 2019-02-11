package ro.edi.xbnr.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ro.edi.xbnr.R
import ro.edi.xbnr.ui.rates.RatesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.exchange_rates).toUpperCase()
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RatesFragment.newInstance())
                .commitNow()
        }

        // FIXME
        // val f: RatesFragment = supportFragmentManager.findFragmentById(R.code.container) as RatesFragment
        toolbar.subtitle = "2019-01-10" // f.ratesModel?.getRates()?.value?.date
    }
}
