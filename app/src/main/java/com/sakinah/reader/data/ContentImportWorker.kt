package com.sakinah.reader.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class ContentImportWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val jsonUrl = inputData.getString(KEY_JSON_URL) ?: return Result.failure()
        val metadataUrl = inputData.getString(KEY_METADATA_URL) ?: return Result.failure()
        val packageId = inputData.getString(KEY_PACKAGE_ID) ?: return Result.failure()
        val expectedHash = inputData.getString(KEY_SHA256)
        return try {
            val rawJson = download(jsonUrl)
            if (expectedHash != null && sha256(rawJson) != expectedHash.lowercase()) return Result.failure()
            val rawMetadata = download(metadataUrl)
            ContentPackageValidator().validate(rawJson)
            ReaderRepository(DatabaseProvider.get(applicationContext), applicationContext)
                .importExternalQuran(rawJson, rawMetadata, packageId, inputData.getString(KEY_TITLE) ?: packageId, inputData.getString(KEY_LICENSE) ?: "Unknown")
            Result.success()
        } catch (error: Throwable) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun download(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("Accept", "application/json")
        connection.connect()
        require(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    companion object {
        const val KEY_JSON_URL = "jsonUrl"
        const val KEY_METADATA_URL = "metadataUrl"
        const val KEY_PACKAGE_ID = "packageId"
        const val KEY_SHA256 = "sha256"
        const val KEY_TITLE = "title"
        const val KEY_LICENSE = "license"
    }
}
