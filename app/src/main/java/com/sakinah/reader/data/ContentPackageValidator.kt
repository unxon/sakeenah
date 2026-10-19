package com.sakinah.reader.data

import org.json.JSONArray

class ContentPackageValidator {
    data class Validated(val chapters: JSONArray, val verseCount: Int)

    fun validate(raw: String, expectedChapters: Int = 114, expectedVerses: Int = 6236): Validated {
        val chapters = JSONArray(raw)
        require(chapters.length() == expectedChapters) { "Invalid chapter count: ${chapters.length()}" }
        var verses = 0
        for (i in 0 until chapters.length()) {
            val chapter = chapters.getJSONObject(i)
            require(chapter.getInt("id") == i + 1) { "Chapter order is invalid at ${i + 1}" }
            val items = chapter.getJSONArray("verses")
            require(items.length() > 0) { "Chapter ${i + 1} has no verses" }
            for (j in 0 until items.length()) {
                val verse = items.getJSONObject(j)
                require(verse.getInt("id") == j + 1) { "Verse order is invalid in chapter ${i + 1}" }
                require(verse.getString("text").isNotBlank()) { "Empty verse in chapter ${i + 1}" }
                verses++
            }
        }
        require(verses == expectedVerses) { "Invalid verse count: $verses" }
        return Validated(chapters, verses)
    }
}
