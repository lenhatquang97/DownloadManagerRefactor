package com.quangln2.downloadmanagerrefactor.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.ServiceLocator
import com.quangln2.downloadmanagerrefactor.ViewModelFactory
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.database.DownloadDatabase
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import com.quangln2.downloadmanagerrefactor.data.source.local.LocalDataSourceImpl
import com.quangln2.downloadmanagerrefactor.data.source.remote.RemoteDataSourceImpl
import com.quangln2.downloadmanagerrefactor.databinding.AddDownloadDialogBinding
import com.quangln2.downloadmanagerrefactor.ui.home.HomeViewModel
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil.Companion.getRealPath

class AddToDownloadDialog : DialogFragment() {
    private lateinit var binding: AddDownloadDialogBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>


    private val database by lazy { DownloadDatabase.getDatabase(requireContext()) }
    val downloadRepository by lazy { ServiceLocator.provideDownloadRepository(database.downloadDao()) }
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(
            DefaultDownloadRepository(
                LocalDataSourceImpl(database.downloadDao()),
                RemoteDataSourceImpl()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result?.data?.data.also { uri ->
                        try {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                val downFile =
                                    uri?.let { DocumentFile.fromTreeUri(requireContext(), it) }
                                binding.downloadToTextField.editText?.setText(getRealPath(downFile))
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AddDownloadDialogBinding.inflate(layoutInflater)
        val spec = CircularProgressIndicatorSpec(
            requireContext(), null, 0,
            com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
        )
        val progressIndicatorDrawable =
            IndeterminateDrawable.createCircularDrawable(requireContext(), spec)

        binding.addNewDownloadFileButton.setOnClickListener {
            val downloadLink = binding.linkTextField.editText?.text.toString()
            val downloadTo = binding.downloadToTextField.editText?.text.toString()
            binding.addNewDownloadFileButton.icon = progressIndicatorDrawable
            val success = viewModel.addNewDownloadInfo(downloadLink, downloadTo)
            if (success) {
                val onHandle: (StructureDownFile) -> Unit = {
                    dismiss()
                    openDownloadDialog(it)
                }
                viewModel.fetchDownloadFileInfo(onHandle)
                closeKeyboard(binding.linkTextField)
            } else {
                binding.addNewDownloadFileButton.icon = null
                Toast.makeText(requireContext(), ConstantClass.INVALID_URL, Toast.LENGTH_SHORT).show()
            }
        }
        binding.cancelAddNewDownloadFileButton.setOnClickListener {
            dismiss()
        }
        binding.endIcon.setOnClickListener {
            getFilePath()
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            binding.downloadToTextField.visibility = View.GONE
            binding.noteAboveAndroid10.visibility = View.VISIBLE
        } else {
            binding.endIcon.inputType = InputType.TYPE_NULL
            binding.downloadToTextField.visibility = View.VISIBLE
            binding.noteAboveAndroid10.visibility = View.GONE
        }
        return AlertDialog.Builder(requireActivity(), R.style.CustomAlertDialog)
            .setView(binding.root).create()
    }



    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view.windowToken != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun getFilePath() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        resultLauncher.launch(intent)
    }

    private fun openDownloadDialog(file: StructureDownFile) {
        if (file.size == -1L) {
            Log.d("DownloadDialog", "File size is -1")
            binding.addNewDownloadFileButton.icon = null
            Toast.makeText(context, ConstantClass.INVALID_LINK, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.preProcessingDownloadFile(requireContext(), file)
    }
}