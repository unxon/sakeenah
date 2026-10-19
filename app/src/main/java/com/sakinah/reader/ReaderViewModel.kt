package com.sakinah.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.reader.data.DatabaseProvider
import com.sakinah.reader.data.PassageEntity
import com.sakinah.reader.data.ReaderRepository
import com.sakinah.reader.data.UserPreferences
import com.sakinah.reader.data.AnnotationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.json.JSONArray
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sakinah.reader.data.SearchIndexWorker
import kotlinx.coroutines.launch

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReaderRepository(DatabaseProvider.get(application), application)
    private val preferences = UserPreferences(application)
    private val selectedChapter = MutableStateFlow(1)
    private val targetVerse = MutableStateFlow(0)
    val chapters: StateFlow<List<Pair<Int, String>>> = MutableStateFlow(loadChapters(application))
    val passages: StateFlow<List<PassageEntity>> = selectedChapter.flatMapLatest { chapter -> repository.passages("quran:uthmani", chapter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val fontSize: StateFlow<Float> = preferences.fontSize.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 25f)

    val saved: StateFlow<Set<Int>> = repository.bookmarks().map { items -> items.filter { it.workId == "quran:uthmani" }.map { it.passageNumber }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val savedPassages: StateFlow<List<PassageEntity>> = combine(passages, saved) { all, ids -> all.filter { ids.contains(it.number) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notes: StateFlow<List<AnnotationEntity>> = repository.annotations()
        .map { items -> items.filter { it.workId == "quran:uthmani" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFontSize(value: Float) { viewModelScope.launch { preferences.setFontSize(value) } }
    fun selectChapter(number: Int) { selectedChapter.value = number }
    val pendingVerse: StateFlow<Int> = targetVerse
    fun openPassage(passage: PassageEntity) { targetVerse.value = passage.verseNumber; selectedChapter.value = passage.chapterNumber }
    fun clearPendingVerse() { targetVerse.value = 0 }

    fun saveProgress(number: Int) { viewModelScope.launch { repository.saveProgress("quran:uthmani", number) } }
    fun saveNote(number: Int, body: String) { if (body.isNotBlank()) viewModelScope.launch { repository.saveNote("quran:uthmani", number, body.trim()) } }
    fun updateNote(note: AnnotationEntity, body: String) { if (body.isNotBlank()) viewModelScope.launch { repository.updateNote(note, body) } }
    fun deleteNote(note: AnnotationEntity) { viewModelScope.launch { repository.deleteNote(note) } }
    fun openNote(note: AnnotationEntity, onReady: (PassageEntity) -> Unit) { viewModelScope.launch { repository.passage(note.workId, note.passageNumber)?.let(onReady) } }

    fun toggleBookmark(number: Int) { viewModelScope.launch { if (saved.value.contains(number)) repository.deleteBookmark("quran:uthmani", number) else repository.saveBookmark("quran:uthmani", number) } }

    init {
        viewModelScope.launch { repository.importBundledQuran() }
        WorkManager.getInstance(application).enqueueUniqueWork(
            "sakinah-search-index",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SearchIndexWorker>().build(),
        )
    }

    private fun loadChapters(application: Application): List<Pair<Int, String>> = runCatching {
        val raw = application.assets.open("content/quran/chapters.json").bufferedReader().use { it.readText() }
        val json = JSONArray(raw)
        (0 until json.length()).map { index -> val item = json.getJSONObject(index); item.getInt("id") to item.getString("name") }
    }.getOrDefault(emptyList())
}
