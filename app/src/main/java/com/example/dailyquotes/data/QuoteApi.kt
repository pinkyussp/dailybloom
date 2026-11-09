package com.example.dailyquotes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Minimal HTTP client for the DummyJSON quotes endpoint. No third-party networking library. */
object QuoteApi {

    private const val RANDOM_QUOTE_URL = "https://dummyjson.com/quotes/random"

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
            Quote.fromApiJson(JSONObject(body))
        } finally {
            connection.disconnect()
        }
    }
}
