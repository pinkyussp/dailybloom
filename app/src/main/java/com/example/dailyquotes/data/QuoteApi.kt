package com.example.dailyquotes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal HTTP client for the Quotable API (https://github.com/lukePeavey/quotable),
 * which serves a library of several thousand quotes from many authors.
 * No third-party networking library.
 */
object QuoteApi {

    private const val RANDOM_QUOTE_URL = "https://api.quotable.io/quotes/random"

    suspend fun fetchRandomQuote(): Quote = withContext(Dispatchers.IO) {
        val connection = URL(RANDOM_QUOTE_URL).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            if (code !in 200..299) throw IOException("Server returned HTTP $code")

            val body = connection.inputStream.bufferedReader().use { it.readText() }

            // Quotable returns an array containing one quote.
            val array = JSONArray(body)
            if (array.length() == 0) throw IOException("No quote returned")
            Quote.fromApiJson(array.getJSONObject(0))
        } finally {
            connection.disconnect()
        }
    }
}
