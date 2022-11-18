package com.deccovers.currencyconverterapp.data

interface ExchangeRepository {

    suspend fun getExchangeRate(currencyInput: String, currencyOutput: String): Double
}