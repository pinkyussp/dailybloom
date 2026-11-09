package com.example.dailyquotes.data

import org.json.JSONObject

data class Quote(
    val id: Int,
    val text: String,
    val author: String
) {
    fun shareText(): String = "“$text” — $author"

    fun toStorageJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("text", text)
        .put("author", author)

    companion object {
        /** Parses the DummyJSON response shape: { "id", "quote", "author" }. */
        fun fromApiJson(json: JSONObject): Quote {
            val text = json.optString("quote").trim()
            require(text.isNotEmpty()) { "Quote text was empty" }
            return Quote(
                id = json.optInt("id"),
                text = text,
                author = json.optString("author").ifBlank { "Unknown" }
            )
        }

        /** Parses the shape this app writes to local storage. Throws JSONException if malformed. */
        fun fromStorageJson(json: JSONObject): Quote = Quote(
            id = json.getInt("id"),
            text = json.getString("text"),
            author = json.getString("author")
        )
    }
}

data class AppSettings(
    val darkTheme: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)
