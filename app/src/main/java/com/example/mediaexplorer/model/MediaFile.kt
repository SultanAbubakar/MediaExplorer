package com.example.mediaexplorer.model

import android.net.Uri

data class MediaFile(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long,
    val dateAdded: Long
)
