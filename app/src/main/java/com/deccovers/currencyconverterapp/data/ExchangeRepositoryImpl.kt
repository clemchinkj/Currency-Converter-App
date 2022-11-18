package com.deccovers.currencyconverterapp.data

import org.jsoup.Jsoup
import org.jsoup.select.Elements

class ExchangeRepositoryImpl : ExchangeRepository {
    override suspend fun getExchangeRate(currencyInput: String, currencyOutput: String): Double {
        if (currencyInput == currencyOutput) {
            return 1.0
        }

        return try {
            val url = "https://www.google.com/search?q=$currencyInput+to+$currencyOutput"
            val doc = Jsoup.connect(url).get()

            val elements: Elements = doc.select("span.DFlfde.SwHCTb")
            val outputString = elements.attr("data-value")

            val exchangeRate = outputString.toDouble()
            exchangeRate
        } catch (e: Exception) {
            e.printStackTrace()
            1.0
        }
    }
}