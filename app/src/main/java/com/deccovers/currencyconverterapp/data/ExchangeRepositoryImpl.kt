package com.deccovers.currencyconverterapp.data

import android.util.Log
import com.deccovers.currencyconverterapp.util.Resource
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class ExchangeRepositoryImpl : ExchangeRepository {
    override suspend fun getExchangeRate(currencyInput: String, currencyOutput: String): Resource<Double> {
        return try {
            val url = "https://www.google.com/search?q=$currencyInput+to+$currencyOutput"
            val doc = Jsoup.connect(url).get()

            val elements: Elements = doc.select("span.DFlfde.SwHCTb")
            val outputString = elements.attr("data-value")

            val exchangeRate = outputString.toDouble()
            Log.d("ExchangeRepositoryImpl", "getExchangeRate: $exchangeRate")
            Resource.Success(exchangeRate)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.toString())
        }
    }

}