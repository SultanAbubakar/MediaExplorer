package com.example.mediaexplorer.paging

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mediaexplorer.model.MediaFile
import com.example.mediaexplorer.viewmodel.FileViewModel.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaPagingSource(
    private val contentResolver: ContentResolver,
    private val collectionUri: Uri,
    private val searchQuery: String,
    private val sortOrder: SortOrder
) : PagingSource<Int, MediaFile>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaFile> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val mediaFiles = withContext(Dispatchers.IO) {
                val result = mutableListOf<MediaFile>()

                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED
                )

                val selection = buildSelection()
                val selectionArgs = buildSelectionArgs()

                val sortBy = when (sortOrder) {
                    SortOrder.NEWEST -> "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
                    SortOrder.OLDEST -> "${MediaStore.Files.FileColumns.DATE_ADDED} ASC"
                    SortOrder.SIZE_LARGE -> "${MediaStore.Files.FileColumns.SIZE} DESC"
                    SortOrder.SIZE_SMALL -> "${MediaStore.Files.FileColumns.SIZE} ASC"
                }

                val cursor: Cursor? = contentResolver.query(
                    collectionUri,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val name = it.getString(nameCol) ?: "Unnamed"
                        val mime = it.getString(mimeCol) ?: "unknown/*"
                        val size = it.getLong(sizeCol)
                        val dateAdded = it.getLong(dateCol)

                        val uri = Uri.withAppendedPath(collectionUri, id.toString())

                        result.add(
                            MediaFile(
                                uri = uri,
                                name = name,
                                mimeType = mime,
                                size = size,
                                dateAdded = dateAdded
                            )
                        )
                    }
                }

                result
            }

            LoadResult.Page(
                data = mediaFiles,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (mediaFiles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaFile>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    private fun buildSelection(): String {
        val mediaTypes = listOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO,
            MediaStore.Files.FileColumns.MEDIA_TYPE_NONE // for docs
        )

        val baseSelection = mediaTypes.joinToString(" OR ") {
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=$it"
        }

        return if (searchQuery.isNotBlank()) {
            "($baseSelection) AND ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        } else {
            baseSelection
        }
    }

    private fun buildSelectionArgs(): Array<String>? {
        return if (searchQuery.isNotBlank()) arrayOf("%$searchQuery%") else null
    }
}
