package com.example.mediaexplorer.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.mediaexplorer.model.MediaFile
import com.example.mediaexplorer.paging.MediaPagingSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FileViewModel(context: Context) : ViewModel() {

    private val contentResolver: ContentResolver = context.contentResolver

    enum class SortOrder {
        NEWEST,
        OLDEST,
        SIZE_SMALL,
        SIZE_LARGE
    }

    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val refreshTrigger = MutableStateFlow(Unit)

    val mediaPagingFlow: Flow<PagingData<MediaFile>> = combine(
        searchQuery, sortOrder, refreshTrigger
    ) { query, order, _ ->
        Triple(query, order, Unit)
    }.flatMapLatest { (query, order, _) ->
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                MediaPagingSource(contentResolver, MediaStore.Files.getContentUri("external"), query, order)
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            searchQuery.emit(query)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            sortOrder.emit(order)
        }
    }

    fun refresh() {
        refreshTrigger.value = Unit
    }
}
