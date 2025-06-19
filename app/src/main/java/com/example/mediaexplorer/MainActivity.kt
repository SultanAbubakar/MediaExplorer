package com.example.mediaexplorer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaexplorer.adapter.MediaFileAdapter
import com.example.mediaexplorer.adapter.MediaLoadStateAdapter
import com.example.mediaexplorer.bottomsheet.MenuBottomSheet
import com.example.mediaexplorer.model.MediaFile
import com.example.mediaexplorer.viewmodel.FileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MediaFileAdapter
    private lateinit var viewModel: FileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = FileViewModel(applicationContext)

        adapter = MediaFileAdapter(
            onItemClick = { openFile(it) },
            onMoreClick = {
                val menuBottomSheet = MenuBottomSheet(it).apply {
                    onShare = {
                        shareFile(it)
                    }

                    onDelete = {
                        deleteFile(it)
                    }
                }

                menuBottomSheet.show(supportFragmentManager, menuBottomSheet.tag)

            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.mediaRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter.withLoadStateFooter(MediaLoadStateAdapter())

        checkPermissions()

        lifecycleScope.launch {
            viewModel.mediaPagingFlow.collectLatest {

                adapter.submitData(it)
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                viewModel.refresh()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }
    }

    private fun openFile(file: MediaFile) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(file.uri, file.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: MediaFile) {
        val share = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, file.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(share, "Share via"))
    }

    private fun deleteFile(file: MediaFile) {
        try {
            contentResolver.delete(file.uri, null, null)
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            viewModel.refresh()
        } catch (e: Exception) {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        }
    }

}
