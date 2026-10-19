package com.sakinah.reader.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.withTransaction

class SearchIndexWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val database = DatabaseProvider.get(applicationContext)
            database.withTransaction {
                val dao = database.readerDao()
                val passages = dao.allPassages()
                dao.clearSearch()
                dao.insertSearch(passages.map { PassageSearchEntity(it.normalizedText, it.workId, it.number) })
            }
            Result.success()
        } catch (error: Throwable) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
