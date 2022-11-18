package com.deccovers.currencyconverterapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deccovers.currencyconverterapp.data.ExchangeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivityViewModel"

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository
): ViewModel() {

    // StateFlow
    private val _exchangeUiState = MutableStateFlow<ExchangeUiState>(ExchangeUiState.Empty)
    val exchangeUiState: StateFlow<ExchangeUiState> = _exchangeUiState
    sealed class ExchangeUiState {
        object Success: ExchangeUiState()
        data class Error(val message: String): ExchangeUiState()
        object Loading: ExchangeUiState()
        object Empty: ExchangeUiState()
    }

    private var exchangeRate = 1.0
    private var cachedCurrencyInput: String? = null
    private var cachedCurrencyOutput: String? = null
    private lateinit var coroutineJob: Job

    fun updateExchangeRate(currencyInput: String, currencyOutput: String) {
        cachedCurrencyInput = currencyInput
        cachedCurrencyOutput = currencyOutput

        coroutineJob = viewModelScope.launch(Dispatchers.IO) {
            _exchangeUiState.value = ExchangeUiState.Loading

            if (currencyInput == currencyOutput) {
                exchangeRate = 1.0
                delay(300L)
                _exchangeUiState.value = ExchangeUiState.Success
                Log.d(TAG, "Currency input = output, rate = $exchangeRate")
            } else {
                exchangeRate = exchangeRepository.getExchangeRate(currencyInput, currencyOutput)
                _exchangeUiState.value = ExchangeUiState.Success
                Log.d(TAG, "Exchange rate retrieved: $exchangeRate")
            }
        }
        // TODO error catch
    }

    fun convert(amount: Double) : Double {
        val outputAmount = amount * exchangeRate
        Log.d(TAG, "Converted $amount to $outputAmount, using rate of $exchangeRate")
        return outputAmount
    }

    // TODO cancel job after response timeout

}