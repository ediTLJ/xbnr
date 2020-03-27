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

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ro.edi.util.onAfterTextChanged
import ro.edi.xbnr.R
import ro.edi.xbnr.databinding.FragmentConverterBinding
import ro.edi.xbnr.ui.viewmodel.ConverterViewModel
import java.math.RoundingMode
import java.text.NumberFormat
import timber.log.Timber.i as logi

// TODO use BigDecimal? do we need that kind of precision?
// TODO fractional digits count on some currencies (e.g. Japanese Yen)... see https://en.wikipedia.org/wiki/ISO_4217
class ConverterFragment : Fragment() {
    companion object {
        const val ARG_FROM_CURRENCY_ID = "ro.edi.xbnr.ui.converter.arg_from_currency_id"
        const val ARG_TO_CURRENCY_ID = "ro.edi.xbnr.ui.converter.arg_to_currency_id"
        const val ARG_DATE = "ro.edi.xbnr.ui.converter.arg_date"

        fun newInstance(fromCurrencyId: Int, toCurrencyId: Int, date: String?) =
            ConverterFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FROM_CURRENCY_ID, fromCurrencyId)
                    putInt(ARG_TO_CURRENCY_ID, toCurrencyId)
                    putString(ARG_DATE, date)
                }
            }
    }

    private lateinit var converterModel: ConverterViewModel
    private lateinit var binding: FragmentConverterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        converterModel = ViewModelProvider(this, factory).get(ConverterViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConverterBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            model = converterModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fromInput = view.findViewById<TextInputLayout>(R.id.from_input)
        fromInput.requestFocus()

        val fromValue = view.findViewById<TextInputEditText>(R.id.from_value)
        val toValue = view.findViewById<TextInputEditText>(R.id.to_value)

        val nf = NumberFormat.getNumberInstance()
        nf.roundingMode =
            RoundingMode.HALF_UP // TODO add a setting for it (HALF_UP, HALF_EVEN & HALF_DOWN options)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2

        // FIXME auto-format input value (including when deleting separators)

        fromValue.onAfterTextChanged { txtValue ->
            if (!fromValue.hasFocus()) {
                return@onAfterTextChanged
            }

            txtValue ?: return@onAfterTextChanged

            if (txtValue.isEmpty()) {
                toValue.setText("")
                return@onAfterTextChanged
            }

            val value = nf.parse(txtValue)?.toDouble() ?: 0.0

            val result = value * converterModel.getRate()
            toValue.setText(nf.format(result))

//            val txtPrevResult = toValue.text.toString()
//            if (txtPrevResult.isEmpty()) {
//                toValue.setText(nf.format(result))
//                return@onAfterTextChanged
//            }
//
//            val prevResult = nf.parse(txtPrevResult)?.toDouble() ?: 0.0
//            if ((prevResult * 100).roundToLong() != (result * 100).roundToLong()) {
//                toValue.setText(nf.format(result))
//            }
        }

        toValue.onAfterTextChanged { txtValue ->
            if (!toValue.hasFocus()) {
                return@onAfterTextChanged
            }

            txtValue ?: return@onAfterTextChanged

            if (txtValue.isEmpty()) {
                fromValue.setText("")
                return@onAfterTextChanged
            }

            val value = nf.parse(txtValue)?.toDouble() ?: 0.0

            val result = value / converterModel.getRate()
            fromValue.setText(nf.format(result))

//            val txtPrevResult = fromValue.text.toString()
//            if (txtPrevResult.isEmpty()) {
//                fromValue.setText(nf.format(result))
//                return@onAfterTextChanged
//            }
//
//            val prevResult = nf.parse(txtPrevResult)?.toDouble() ?: 0.0
//            if ((prevResult * 100).roundToLong() != (result * 100).roundToLong()) {
//                fromValue.setText(nf.format(result))
//            }
        }

        converterModel.fromCurrency.observe(viewLifecycleOwner, Observer { currency ->
            logi("converterModel from currency changed: $currency")

            if (currency == null) {
                return@Observer
            }
        })

        converterModel.toCurrency.observe(viewLifecycleOwner, Observer { currency ->
            logi("converterModel to currency changed: $currency")

            if (currency == null) {
                return@Observer
            }
        })

        converterModel.currencies.observe(viewLifecycleOwner, Observer { currencies ->
            logi("converterModel currencies changed")

            binding.invalidateAll()

            if (currencies.isNullOrEmpty()) {
                return@Observer
            }

            // TODO init spinners

            val imm =
                view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        })
    }

    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ConverterViewModel(
                (activity as AppCompatActivity).application,
                arguments?.getInt(ARG_FROM_CURRENCY_ID, 0) ?: 0,
                arguments?.getInt(ARG_TO_CURRENCY_ID, 0) ?: 0,
                arguments?.getString(ARG_DATE)
            ) as T
        }
    }
}