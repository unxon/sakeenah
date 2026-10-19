package com.sakinah.reader.data

import android.content.Context
import org.json.JSONObject

data class ContentCategory(val id: String, val title: String, val type: String, val sourceFile: String)
data class ContentWork(val id: String, val categoryId: String, val title: String, val subtitle: String, val sourceFile: String, val verseCount: Int)

class ContentCatalogRepository(private val context: Context) {
    fun load(): Pair<List<ContentCategory>, List<ContentWork>> {
        val raw = context.assets.open("content/catalog.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        val categories = buildList {
            val array = root.getJSONArray("categories")
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(ContentCategory(item.getString("id"), item.getString("title"), item.getString("type"), item.getString("sourceFile")))
            }
        }
        val works = buildList {
            val array = root.getJSONArray("works")
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(ContentWork(item.getString("id"), item.getString("categoryId"), item.getString("title"), item.optString("subtitle"), item.getString("sourceFile"), item.getInt("verseCount")))
            }
        }
        return categories to works
    }
}
