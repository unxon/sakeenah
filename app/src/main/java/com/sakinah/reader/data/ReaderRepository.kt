package com.sakinah.reader.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONArray
import java.security.MessageDigest

class ReaderRepository(private val database: ReaderDatabase, private val context: Context) {
    private val dao = database.readerDao()
    fun passages(workId: String, chapterNumber: Int): Flow<List<PassageEntity>> = dao.passagesForChapter(workId, chapterNumber)
    fun bookmarks(): Flow<List<BookmarkEntity>> = dao.bookmarks()
    fun annotations(): Flow<List<AnnotationEntity>> = dao.annotations()
    suspend fun search(query: String): List<PassageEntity> = dao.searchFts(normalize(query))
    suspend fun saveBookmark(workId: String, passageNumber: Int) = dao.saveBookmark(BookmarkEntity(workId, passageNumber, System.currentTimeMillis()))
    suspend fun deleteBookmark(workId: String, passageNumber: Int) = dao.deleteBookmark(workId, passageNumber)
    suspend fun saveProgress(workId: String, passageNumber: Int) = dao.saveProgress(ReadingProgressEntity(workId, passageNumber, System.currentTimeMillis()))
    suspend fun saveNote(workId: String, passageNumber: Int, body: String) = dao.saveAnnotation(AnnotationEntity("$workId:$passageNumber:${System.currentTimeMillis()}", workId, passageNumber, body, System.currentTimeMillis()))
    suspend fun updateNote(note: AnnotationEntity, body: String) = dao.saveAnnotation(note.copy(body = body.trim(), updatedAt = System.currentTimeMillis()))
    suspend fun deleteNote(note: AnnotationEntity) = dao.saveAnnotation(note.copy(deleted = true, updatedAt = System.currentTimeMillis()))
    suspend fun passage(workId: String, number: Int): PassageEntity? = dao.passage(workId, number)

    suspend fun importBundledQuran() = withContext(Dispatchers.IO) {
        val workId = "quran:uthmani"
        val quranJson = context.assets.open("content/quran/quran-uthmani.json").bufferedReader().use { it.readText() }
        val chaptersJson = context.assets.open("content/quran/chapters.json").bufferedReader().use { it.readText() }
        importValidatedInBatches(quranJson, chaptersJson, "quran-json:uthmani", "القرآن الكريم — الرسم العثماني", "CC BY 3.0 / Tanzil")
    }

    private suspend fun importValidatedInBatches(rawQuran: String, rawMetadata: String, packageId: String, title: String, license: String) {
        val metadata = JSONArray(rawMetadata)
        val chapters = ContentPackageValidator().validate(rawQuran).chapters
        val total = 6236
        if (dao.passageCount(packageId) >= total) return
        val hash = sha256(rawQuran)
        database.withTransaction {
            dao.insertPackage(ContentPackageEntity(packageId, 1, title, "ar", license, hash, true))
            dao.insertWork(WorkEntity(packageId, packageId, title, "رواية حفص · الرسم العثماني", total))
        }
        var globalNumber = 0
        for (i in 0 until chapters.length()) {
            val chapter = chapters.getJSONObject(i)
            val chapterMeta = metadata.getJSONObject(i)
            val chapterNumber = chapter.getInt("id")
            val chapterTitle = chapterMeta.getString("name")
            val verses = chapter.getJSONArray("verses")
            val chapterPassages = ArrayList<PassageEntity>(verses.length())
            for (j in 0 until verses.length()) {
                globalNumber++
                val verse = verses.getJSONObject(j)
                val text = verse.getString("text")
                chapterPassages += PassageEntity(packageId, globalNumber, text, normalize(text), packageId, chapterNumber, verse.getInt("id"), chapterTitle)
            }
            database.withTransaction {
                dao.insertPassages(chapterPassages)
                dao.insertSearch(chapterPassages.map { PassageSearchEntity(it.normalizedText, it.workId, it.number) })
                if (i == 0) dao.saveProgress(ReadingProgressEntity(packageId, 1, System.currentTimeMillis()))
            }
            yield()
        }
    }

    suspend fun importExternalQuran(rawQuran: String, rawMetadata: String, packageId: String, title: String, license: String) = withContext(Dispatchers.IO) {
        importValidatedInBatches(rawQuran, rawMetadata, packageId, title, license)
    }

    private fun sha256(text: String): String = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
    private fun normalize(value: String): String = value.replace(Regex("[ًٌٍَُِّْـ]"), "").replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا').replace('ى', 'ي')
}
