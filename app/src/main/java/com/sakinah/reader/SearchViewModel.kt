package com.sakinah.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.reader.data.DatabaseProvider
import com.sakinah.reader.data.PassageEntity
import com.sakinah.reader.data.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReaderRepository(DatabaseProvider.get(application), application)
    private val query = MutableStateFlow("")
    val results: StateFlow<List<PassageEntity>> = query
        .debounce(120)
        .distinctUntilChanged()
        .flatMapLatest { value -> flow { emit(if (value.isBlank()) emptyList() else repository.search(normalize(value))) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateQuery(value: String) { query.value = value }
    private fun normalize(value: String) = value.replace(Regex("[ًٌٍَُِّْـ]"), "").replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ى', 'ي')
}
