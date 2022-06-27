package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel
import kotlinx.coroutines.*

class AddToDownloadDialog: DialogFragment() {
    private lateinit var binding: AddDownloadDialogBinding
    private val viewModel: HomeViewModel by activityViewModels { ViewModelFactory(DefaultDownloadRepository(), requireContext()) }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        viewModel.fetchedFileInfo.observe(this) {
            file ->
            if(file != null && file.downloadLink != "test"){
                showDownloadAlertDialog(file)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AddDownloadDialogBinding.inflate(layoutInflater)
        binding.addNewDownloadFileButton.setOnClickListener {
            viewModel.addNewDownloadInfo(binding.linkTextField.editText?.text.toString(), binding.downloadToTextField.editText?.text.toString())
            viewModel.fetchDownloadFileInfo()
            closeKeyboard(binding.linkTextField)
        }
        binding.cancelAddNewDownloadFileButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(binding.root).create()
    }

    private fun showDownloadAlertDialog(file: StrucDownFile) {
        val builder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogShow)
                .setTitle(file.fileName)
                .setIcon(R.drawable.ic_baseline_arrow_downward_24)
                .setMessage("Do you want to download this file? This will cost ${file.convertToSizeUnit()}.")
                .setPositiveButton("OK") { _, _ ->
                    viewModel.downloadAFile()
                    dismiss()
                }
                .setNegativeButton("CANCEL") { _, _ ->
                    file.downloadLink = "test"
                    dismiss()
                }
        builder.show()
        dismiss()
    }
    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view.windowToken != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}