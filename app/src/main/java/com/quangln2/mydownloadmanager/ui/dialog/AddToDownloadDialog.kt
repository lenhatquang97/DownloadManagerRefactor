package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.flow.collect

class AddToDownloadDialog: DialogFragment() {
    private lateinit var binding: AddDownloadDialogBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AddDownloadDialogBinding.inflate(LayoutInflater.from(context))
        binding.addNewDownloadFileButton.setOnClickListener {
            viewModel.addNewDownloadInfo(binding.linkTextField.editText?.text.toString(), binding.downloadToTextField.editText?.text.toString())
            dismiss()
            //lifecycleScope.launchWhenStarted {
                showDownloadAlertDialog()
            //}
        }
        binding.cancelAddNewDownloadFileButton.setOnClickListener {
            dismiss()
        }

        val downloadViewModelFactory = ViewModelFactory(DefaultDownloadRepository(), context!!)
        viewModel = activity?.run {
            ViewModelProvider(this, downloadViewModelFactory).get(HomeViewModel::class.java)
        }!!

        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog).setView(binding.root).create()
    }

    fun showDownloadAlertDialog() {
        lifecycleScope.launch {
            viewModel.fetchDownloadInfoToUI()
            val builder =
                MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogShow)
                    .setTitle(viewModel._item.value?.fileName)
                    .setIcon(R.drawable.ic_baseline_arrow_downward_24)
                    .setMessage("Do you want to download this file? This will cost ${viewModel._item.value?.convertToSizeUnit()}.")
                    .setPositiveButton("OK") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.downloadAFile()
                        }
                        dismiss()
                    }
                    .setNegativeButton("CANCEL") { _, _ ->
                        dismiss()
                    }
            builder.show()
        }

    }


}