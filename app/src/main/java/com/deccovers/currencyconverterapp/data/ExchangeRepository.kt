package com.deccovers.currencyconverterapp.data

import com.deccovers.currencyconverterapp.util.Resource

interface ExchangeRepository {

    suspend fun getExchangeRate(currencyInput: String, currencyOutput: String): Resource<Double>
}