package com.deccovers.currencyconverterapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.deccovers.currencyconverterapp.R
import com.deccovers.currencyconverterapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private var spinnerLastStableValues = intArrayOf(0,0)

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etInputAmount.textChanges()
            .debounce(300)
//            .filterNot { it.isNullOrBlank() }
            .onEach {
                // TODO try out map function
                if (it.toString().startsWith('.')) {
                    val outputAmount = mainActivityViewModel.convert("0${it.toString()}".toDouble())
                    binding.tvResult.text = outputAmount.roundDecimal(2)
                } else if (it.isNullOrBlank()) {
                    binding.tvResult.text = ""
                } else {
                    val outputAmount = mainActivityViewModel.convert(it.toString().toDouble())
                    binding.tvResult.text = outputAmount.roundDecimal(2)
                }
            }
            .launchIn(lifecycleScope)

        binding.spinnerInputCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    mainActivityViewModel.updateExchangeRate(
                        p0?.selectedItem.toString(),
                        binding.spinnerOutputCurrency.selectedItem.toString()
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        binding.spinnerOutputCurrency.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    mainActivityViewModel.updateExchangeRate(
                        binding.spinnerInputCurrency.selectedItem.toString(),
                        p0?.selectedItem.toString()
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }

        // Use launchWhenStarted here so that UI updates only happen when app is started
        lifecycleScope.launchWhenStarted {
            mainActivityViewModel.exchangeUiState.collect {
                when (it) {
                    is MainActivityViewModel.ExchangeUiState.Success -> {
                        Log.d(TAG, "ExchangeUiState.Success")
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.spinnerInputCurrency.isEnabled = true
                        binding.spinnerOutputCurrency.isEnabled = true
                        binding.etInputAmount.isEnabled = true
                        if (binding.etInputAmount.text.isNotEmpty()) {
                            val outputAmount = mainActivityViewModel.convert(
                                binding.etInputAmount.text.toString().toDouble()
                            )
                            binding.tvResult.text = outputAmount.roundDecimal(2)
                        } else {
                            binding.tvResult.text = ""
                        }

                        spinnerLastStableValues[0] = binding.spinnerInputCurrency.selectedItemPosition
                        spinnerLastStableValues[1] = binding.spinnerOutputCurrency.selectedItemPosition
                        Log.d(TAG, "Updated spinner positions to ${spinnerLastStableValues[0]} and ${spinnerLastStableValues[1]}")
                    }

                    is MainActivityViewModel.ExchangeUiState.Error -> {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.spinnerInputCurrency.isEnabled = true
                        binding.spinnerOutputCurrency.isEnabled = true
                        binding.etInputAmount.isEnabled = true

                        binding.tvResult.text = ""
                        binding.spinnerInputCurrency.setSelection(spinnerLastStableValues[0])
                        binding.spinnerOutputCurrency.setSelection(spinnerLastStableValues[1])

                        Log.d(TAG, "Reverted spinner positions to ${spinnerLastStableValues[0]} and ${spinnerLastStableValues[1]}")
                        Snackbar.make(binding.root, "Check network connection", Snackbar.LENGTH_LONG).show()

                        Log.e(TAG, "ExchangeUiState.Error")
                    }

                    is MainActivityViewModel.ExchangeUiState.Loading -> {
                        Log.d(TAG, "ExchangeUiState.Loading")
                        binding.progressBar.visibility = View.VISIBLE
                        binding.spinnerInputCurrency.isEnabled = false
                        binding.spinnerOutputCurrency.isEnabled = false
                        binding.etInputAmount.isEnabled = false
                        binding.tvResult.text = getString(R.string.retrieving_rate)
                    }

                    is MainActivityViewModel.ExchangeUiState.Empty -> Unit

                }
            }
        }
    }

    // Kotlin Extensions
    private fun Double.roundDecimal(digit: Int) = "%.${digit}f".format(this)

    private fun EditText.textChanges(): Flow<CharSequence?> {
        return callbackFlow {
            val listener = object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) = Unit

                override fun afterTextChanged(s: Editable?) = Unit
                override fun onTextChanged(
                    charSequence: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    trySend(charSequence)
                }
            }

            addTextChangedListener(listener)
            awaitClose { removeTextChangedListener(listener) }
        }.onStart { emit(text) }
    }

    // TODO separate layout for landscape orientation
}