package com.sakinah.reader.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS content_packages (id TEXT NOT NULL PRIMARY KEY, version INTEGER NOT NULL, title TEXT NOT NULL, language TEXT NOT NULL, license TEXT NOT NULL, sha256 TEXT NOT NULL, verified INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS works (id TEXT NOT NULL, packageId TEXT NOT NULL, title TEXT NOT NULL, subtitle TEXT, passageCount INTEGER NOT NULL, PRIMARY KEY(id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS passagesearch (text TEXT NOT NULL, workId TEXT NOT NULL, passageNumber INTEGER NOT NULL, PRIMARY KEY(workId, passageNumber))")
            db.execSQL("INSERT INTO passagesearch(text, workId, passageNumber) SELECT originalText, workId, number FROM passages")
        }
    }
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE passages ADD COLUMN chapterNumber INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE passages ADD COLUMN verseNumber INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE passages ADD COLUMN chapterTitle TEXT NOT NULL DEFAULT ''")
        }
    }

    @Volatile private var instance: ReaderDatabase? = null
    fun get(context: Context): ReaderDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context.applicationContext, ReaderDatabase::class.java, "sakinah_reader.db")
            .addMigrations(migration1To2, migration2To3)
            .build().also { instance = it }
    }
}
