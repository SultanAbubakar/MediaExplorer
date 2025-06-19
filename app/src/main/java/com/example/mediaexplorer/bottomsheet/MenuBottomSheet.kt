package com.example.mediaexplorer.bottomsheet

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mediaexplorer.R
import com.example.mediaexplorer.databinding.BottomSheetFileOptionsBinding
import com.example.mediaexplorer.model.MediaFile
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DecimalFormat


class MenuBottomSheet(private val mediaFile: MediaFile) : BottomSheetDialogFragment() {

    var onDelete: ((MediaFile) -> Unit)? = null
    var onShare: ((MediaFile) -> Unit)? = null


    private lateinit var binding: BottomSheetFileOptionsBinding
    private lateinit var activity: Activity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.fileNameTextView.text = mediaFile.name
        binding.filePathTextView.text = mediaFile.uri.toString()
        binding.fileSizeTextView.text = formatFileSize(mediaFile.size)

        binding.deleteOption.setOnClickListener {
            onDelete?.let { it1 -> it1(mediaFile) }
            dismiss()
        }

        binding.shareOption.setOnClickListener {
            onShare?.let { it1 -> it1(mediaFile) }
            dismiss()
        }




    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = BottomSheetFileOptionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun getTheme(): Int {
        return R.style.bottomSheetRounded
    }

  private  fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val df = DecimalFormat("#.##")
        return when {
            mb >= 1 -> "${df.format(mb)} MB"
            kb >= 1 -> "${df.format(kb)} KB"
            else -> "$size B"
        }
    }

}