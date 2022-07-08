package com.quangln2.mydownloadmanager.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.databinding.FragmentSecondBinding
import com.quangln2.mydownloadmanager.util.UIComponentUtil

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if(result.resultCode == Activity.RESULT_OK){
                result?.data?.data.also {
                        uri ->
                    try{
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                            val downFile = uri?.let { DocumentFile.fromTreeUri(requireContext(), it) }
                            binding.folderForFilesValue.text = UIComponentUtil.getRealPath(downFile)
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }

            }
        }

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        binding.popUpMessagesSwitch.apply {
            isChecked = GlobalSettings.showPopUpMessage
            setOnClickListener {
                GlobalSettings.openClosePopUpMessage()
                println(GlobalSettings.showPopUpMessage)
            }
        }

        binding.showValueSwitch.apply {
            isChecked = GlobalSettings.showOnLockScreen
            setOnClickListener {
                GlobalSettings.openCloseLockScreen()
                println(GlobalSettings.showOnLockScreen)
            }
        }

        binding.vibrationSwitch.apply {
            isChecked = GlobalSettings.isVibrated
            setOnClickListener {
                GlobalSettings.openCloseVibrated()
                println(GlobalSettings.isVibrated)
            }
        }

        binding.folderForFiles.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                resultLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Not support from API 29", Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}