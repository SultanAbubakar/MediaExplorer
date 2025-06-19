package com.example.mediaexplorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mediaexplorer.R
import com.example.mediaexplorer.model.MediaFile

class MediaFileAdapter(
    private val onItemClick: (MediaFile) -> Unit,
    private val onMoreClick: (MediaFile) -> Unit
) : PagingDataAdapter<MediaFile, MediaFileAdapter.MediaFileViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean =
            oldItem.uri == newItem.uri

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: MediaFile, newItem: MediaFile): Any? {
            return if (oldItem.name != newItem.name) "NAME_CHANGED" else null
        }
    }

    inner class MediaFileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val thumbnail: ImageView = view.findViewById(R.id.thumbnailImageView)
        private val name: TextView = view.findViewById(R.id.fileNameTextView)
        private val mime: TextView = view.findViewById(R.id.mimeTypeTextView)
        private val more: ImageView = view.findViewById(R.id.moreOptionsImageView)

        fun bind(file: MediaFile) {
            name.text = file.name
            mime.text = file.mimeType

            if (file.mimeType.startsWith("image") || file.mimeType.startsWith("video")) {

                thumbnail.load(file.uri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                }

            } else if (file.mimeType.startsWith("audio")) {
                thumbnail.setImageResource(R.drawable.ic_audio)
            } else {
                thumbnail.setImageResource(R.drawable.ic_document)
            }

            itemView.setOnClickListener { onItemClick(file) }
            more.setOnClickListener { onMoreClick( file) }
        }

        fun updateName(newName: String) {
            name.text = newName
        }
    }

    override fun onBindViewHolder(holder: MediaFileViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: MediaFileViewHolder, position: Int, payloads: MutableList<Any>) {
        val file = getItem(position) ?: return
        if (payloads.contains("NAME_CHANGED")) {
            holder.updateName(file.name)
        } else {
            holder.bind(file)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_file, parent, false)
        return MediaFileViewHolder(view)
    }
}
