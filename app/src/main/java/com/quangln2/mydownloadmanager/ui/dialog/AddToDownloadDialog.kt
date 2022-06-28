package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel


class AddToDownloadDialog: DialogFragment() {
    private lateinit var binding: AddDownloadDialogBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: HomeViewModel by activityViewModels { ViewModelFactory(DefaultDownloadRepository(DownloadManagerApplication().database.downloadDao()), requireContext()) }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        viewModel.fetchedFileInfo.observe(this) {
            file ->
            if(file != null && file.downloadLink != "test"){
                showDownloadAlertDialog(file)
            }
        }
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data: Intent? = result.data
                val uri = data?.data
                if(uri != null && context != null){
                    val df = DocumentFile.fromTreeUri(context!!, uri)
                    println(df?.uri?.path)
                    println(Environment.DIRECTORY_DOWNLOADS)
                    binding.downloadToTextField.editText?.setText(getRealPath(df))
                }

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
        binding.downloadToTextField.setOnClickListener {
            getFilePath()
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
    private fun getFilePath(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        resultLauncher.launch(intent)
    }
    private fun getRealPath(treeUri: DocumentFile?): String {
        if (treeUri == null) return ""
        val path1: String = treeUri.uri.path!!
        if (path1.startsWith("/tree/")) {
            val path2 = path1.removeRange(0, "/tree/".length)
            if (path2.startsWith("primary:")) {
                val primary = path2.removeRange(0, "primary:".length)
                if (primary.contains(':')) {
                    val storeName = "/storage/emulated/0/"
                    val last = path2.split(':').last()
                    return storeName + last
                }
            } else {
                if (path2.contains(':')) {
                    val path3 = path2.split(':').first()
                    val last = path2.split(':').last()
                    return "/$path3/$last"
                }
            }
        }
        return path1
    }


}