package com.sakinah.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sakinah.reader.data.ContentCatalogRepository
import com.sakinah.reader.data.ContentWork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val _works = MutableStateFlow<List<ContentWork>>(emptyList())
    val works: StateFlow<List<ContentWork>> = _works

    init { _works.value = runCatching { ContentCatalogRepository(application).load().second }.getOrDefault(emptyList()) }
}
