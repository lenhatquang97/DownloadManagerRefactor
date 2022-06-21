package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.getViewModelFactory
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel

class AddToDownloadDialog: DialogFragment() {
    private var _binding: AddDownloadDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels<HomeViewModel> {getViewModelFactory(context!!)}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = AddDownloadDialogBinding.inflate(LayoutInflater.from(context))


        binding.addNewDownloadFileButton.setOnClickListener {
            viewModel.addNewDownloadInfo(binding.linkTextField.editText?.text.toString(), binding.downloadToTextField.editText?.text.toString())
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
    fun showDownloadAlertDialog() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            viewModel.fetchDownloadInfoToUI()
        }
        println(viewModel.item.value?.downloadLink)
    }
//        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogShow)
//            .setTitle(viewModel.item.value?.fileName)
//            .setIcon(R.drawable.ic_baseline_arrow_downward_24)
//            .setMessage("Do you want to download this file? This will cost ${viewModel.item.value?.convertToSizeUnit()}.")
//            .setPositiveButton("OK") { _, _ ->
//                dismiss()
//            }
//            .setNegativeButton("CANCEL") { _, _ ->
//                dismiss()
//            }
//        if(viewModel.item.value?.size!! > 0L) {
//            builder.show()
//        }
}