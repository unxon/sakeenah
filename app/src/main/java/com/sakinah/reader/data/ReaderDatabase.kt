package com.sakinah.reader.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "content_packages")
data class ContentPackageEntity(@androidx.room.PrimaryKey val id: String, val version: Int, val title: String, val language: String, val license: String, val sha256: String, val verified: Boolean)

@Entity(tableName = "works", primaryKeys = ["id"])
data class WorkEntity(val id: String, val packageId: String, val title: String, val subtitle: String?, val passageCount: Int)

@Entity(tableName = "passages", primaryKeys = ["workId", "number"])
data class PassageEntity(val workId: String, val number: Int, val originalText: String, val normalizedText: String, val sourcePackageId: String, val chapterNumber: Int = 1, val verseNumber: Int = 1, val chapterTitle: String = "")

@Entity(tableName = "passagesearch", primaryKeys = ["workId", "passageNumber"])
data class PassageSearchEntity(val text: String, val workId: String, val passageNumber: Int)

@Entity(tableName = "bookmarks", primaryKeys = ["workId", "passageNumber"])
data class BookmarkEntity(val workId: String, val passageNumber: Int, val createdAt: Long)

@Entity(tableName = "annotations", primaryKeys = ["id"])
data class AnnotationEntity(val id: String, val workId: String, val passageNumber: Int, val body: String, val updatedAt: Long, val deleted: Boolean = false)

@Entity(tableName = "reading_progress", primaryKeys = ["workId"])
data class ReadingProgressEntity(val workId: String, val passageNumber: Int, val updatedAt: Long)

@Dao
interface ReaderDao {
    @Query("SELECT * FROM passages WHERE workId = :workId AND chapterNumber = :chapterNumber ORDER BY verseNumber") fun passagesForChapter(workId: String, chapterNumber: Int): Flow<List<PassageEntity>>
    @Query("SELECT COUNT(*) FROM passages WHERE workId = :workId") suspend fun passageCount(workId: String): Int
    @Query("SELECT * FROM passages ORDER BY workId, number") suspend fun allPassages(): List<PassageEntity>
    @Query("DELETE FROM passagesearch") suspend fun clearSearch()
    @Query("SELECT p.* FROM passages p JOIN passagesearch s ON s.workId = p.workId AND s.passageNumber = p.number WHERE s.text LIKE '%' || :query || '%' ORDER BY p.number LIMIT 50") suspend fun searchFts(query: String): List<PassageEntity>
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC") fun bookmarks(): Flow<List<BookmarkEntity>>
    @Query("SELECT * FROM annotations WHERE deleted = 0 ORDER BY updatedAt DESC") fun annotations(): Flow<List<AnnotationEntity>>
    @Query("SELECT * FROM passages WHERE workId = :workId AND number = :number LIMIT 1") suspend fun passage(workId: String, number: Int): PassageEntity?
    @Query("SELECT * FROM reading_progress WHERE workId = :workId LIMIT 1") suspend fun progress(workId: String): ReadingProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertPackage(item: ContentPackageEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWork(item: WorkEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertPassages(items: List<PassageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSearch(items: List<PassageSearchEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveBookmark(item: BookmarkEntity)
    @Query("DELETE FROM bookmarks WHERE workId = :workId AND passageNumber = :passageNumber") suspend fun deleteBookmark(workId: String, passageNumber: Int)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveAnnotation(item: AnnotationEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProgress(item: ReadingProgressEntity)
}

@Database(entities = [ContentPackageEntity::class, WorkEntity::class, PassageEntity::class, PassageSearchEntity::class, BookmarkEntity::class, AnnotationEntity::class, ReadingProgressEntity::class], version = 3, exportSchema = true)
abstract class ReaderDatabase : RoomDatabase() { abstract fun readerDao(): ReaderDao }
