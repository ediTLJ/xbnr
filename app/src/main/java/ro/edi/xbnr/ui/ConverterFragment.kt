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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ro.edi.util.onAfterTextChanged
import ro.edi.xbnr.databinding.FragmentConverterBinding
import ro.edi.xbnr.ui.viewmodel.ConverterViewModel
import java.math.RoundingMode
import java.text.NumberFormat
import timber.log.Timber.Forest.i as logi

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

    private val converterModel: ConverterViewModel by viewModels { ConverterViewModel.FACTORY }

    private var _binding: FragmentConverterBinding? = null

    // this property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        converterModel.apply {
            fromCurrencyId = arguments?.getInt(ARG_FROM_CURRENCY_ID, 0) ?: 0
            toCurrencyId = arguments?.getInt(ARG_TO_CURRENCY_ID, 0) ?: 0
            date = arguments?.getString(ARG_DATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConverterBinding.inflate(inflater, container, false)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = converterModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fromInput.requestFocus()

        val nf = NumberFormat.getNumberInstance()
        nf.roundingMode =
            RoundingMode.HALF_UP // TODO add a setting for it (HALF_UP, HALF_EVEN & HALF_DOWN options)
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2

        // FIXME auto-format input value (including when deleting separators)

        binding.fromValue.onAfterTextChanged { txtValue ->
            if (!binding.fromValue.hasFocus()) {
                return@onAfterTextChanged
            }

            txtValue ?: return@onAfterTextChanged

            if (txtValue.isEmpty()) {
                binding.toValue.setText("")
                return@onAfterTextChanged
            }

            val value = nf.parse(txtValue)?.toDouble() ?: 0.0

            val result = value * converterModel.getRate()
            binding.toValue.setText(nf.format(result))

//            val txtPrevResult = binding.toValue.text.toString()
//            if (txtPrevResult.isEmpty()) {
//                binding.toValue.setText(nf.format(result))
//                return@onAfterTextChanged
//            }
//
//            val prevResult = nf.parse(txtPrevResult)?.toDouble() ?: 0.0
//            if ((prevResult * 100).roundToLong() != (result * 100).roundToLong()) {
//                binding.toValue.setText(nf.format(result))
//            }
        }

        binding.toValue.onAfterTextChanged { txtValue ->
            if (!binding.toValue.hasFocus()) {
                return@onAfterTextChanged
            }

            txtValue ?: return@onAfterTextChanged

            if (txtValue.isEmpty()) {
                binding.fromValue.setText("")
                return@onAfterTextChanged
            }

            val value = nf.parse(txtValue)?.toDouble() ?: 0.0

            val result = value / converterModel.getRate()
            binding.fromValue.setText(nf.format(result))

//            val txtPrevResult = binding.fromValue.text.toString()
//            if (txtPrevResult.isEmpty()) {
//                binding.fromValue.setText(nf.format(result))
//                return@onAfterTextChanged
//            }
//
//            val prevResult = nf.parse(txtPrevResult)?.toDouble() ?: 0.0
//            if ((prevResult * 100).roundToLong() != (result * 100).roundToLong()) {
//                binding.fromValue.setText(nf.format(result))
//            }
        }

        converterModel.fromCurrency.observe(viewLifecycleOwner) { currency ->
            logi("converterModel from currency changed: $currency")

            if (currency == null) {
                return@observe
            }
        }

        converterModel.toCurrency.observe(viewLifecycleOwner) { currency ->
            logi("converterModel to currency changed: $currency")

            if (currency == null) {
                return@observe
            }
        }

        converterModel.currencies.observe(viewLifecycleOwner) { currencies ->
            logi("converterModel currencies changed")

            binding.invalidateAll()

            if (currencies.isNullOrEmpty()) {
                return@observe
            }

            // TODO init spinners

//            val imm =
//                view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }
}