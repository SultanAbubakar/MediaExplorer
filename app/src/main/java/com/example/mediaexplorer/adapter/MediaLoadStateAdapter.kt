package com.example.mediaexplorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaexplorer.R

class MediaLoadStateAdapter : LoadStateAdapter<MediaLoadStateAdapter.LoadStateViewHolder>() {

    class LoadStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progress: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val errorText: TextView = itemView.findViewById(R.id.errorText)

        fun bind(loadState: LoadState) {
            progress.isVisible = loadState is LoadState.Loading
            errorText.isVisible = loadState is LoadState.Error
            errorText.text = (loadState as? LoadState.Error)?.error?.localizedMessage ?: "Unknown error"
        }
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state_footer, parent, false)
        return LoadStateViewHolder(view)
    }
}
