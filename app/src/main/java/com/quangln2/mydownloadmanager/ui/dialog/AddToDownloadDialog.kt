package com.quangln2.mydownloadmanager.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.ui.home.HomeViewModel


class AddToDownloadDialog: DialogFragment() {
    private lateinit var binding: AddDownloadDialogBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>


    private val database by lazy{ DownloadDatabase.getDatabase(requireContext())}
    val downloadRepository by lazy{ ServiceLocator.provideDownloadRepository(database.downloadDao())}
    private val viewModel: HomeViewModel by activityViewModels { ViewModelFactory(DefaultDownloadRepository(database.downloadDao()), requireContext()) }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        viewModel.fetchedFileInfo.observe(this) {
            file ->
            if(file != null && file.downloadLink != "test" && viewModel._isOpenDialog.value!!){
                showDownloadAlertDialog(file)
            }
            viewModel._isOpenDialog.value = false
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                result?.data?.data.also {
                    uri ->
                    try{
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                            val downFile = uri?.let { DocumentFile.fromTreeUri(requireContext(), it) }
                            println("Path: " + downFile?.uri?.path)
                            binding.downloadToTextField.editText?.setText(getRealPath(downFile))
                            println("Another path: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
                        }

                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AddDownloadDialogBinding.inflate(layoutInflater)
        binding.addNewDownloadFileButton.setOnClickListener {
            viewModel.addNewDownloadInfo(binding.linkTextField.editText?.text.toString(), binding.downloadToTextField.editText?.text.toString())
            viewModel.fetchDownloadFileInfo()
            viewModel._isOpenDialog.value = true
            closeKeyboard(binding.linkTextField)

        }
        binding.cancelAddNewDownloadFileButton.setOnClickListener {
            dismiss()
        }
        binding.endIcon.setOnClickListener {
            getFilePath()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            binding.downloadToTextField.visibility = View.GONE
        } else {
            binding.endIcon.inputType = InputType.TYPE_NULL
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