package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import kotlinx.coroutines.NonCancellable.cancel

class AddToDownloadDialog: DialogFragment() {
    private var _binding: AddDownloadDialogBinding? = null
    private val binding get() = _binding!!
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddDownloadDialogBinding.inflate(LayoutInflater.from(context))

        binding.addNewDownloadFileButton.setOnClickListener {
            showDownloadAlertDialog()
            dismiss()
        }
        binding.cancelAddNewDownloadFileButton.setOnClickListener {
            dismiss()
        }
        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(binding.root).create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun showDownloadAlertDialog(){
        MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogShow)
            .setTitle("test.docx")
            .setIcon(R.drawable.ic_baseline_arrow_downward_24)
            .setMessage("Do you want to download this file? This will cost 30KB.")
            .setPositiveButton("OK") { _, _ ->
                dismiss()
            }
            .setNegativeButton("CANCEL") { _, _ ->
                dismiss()
            }
            .show()
    }
}