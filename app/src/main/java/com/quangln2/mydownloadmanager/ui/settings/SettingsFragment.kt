package com.quangln2.mydownloadmanager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
private const val TAG = "SettingsFragment"
class SettingsFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.popUpMessagesSwitch.apply {
            CoroutineScope(Dispatchers.Main).launch {
                GlobalSettings.getPopUpMessage(requireContext()).collect {
                    isChecked = it
                }
            }
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    GlobalSettings.setPopUpMessage(requireContext(), isChecked)
                }
            }
        }

        binding.showValueSwitch.apply {
            CoroutineScope(Dispatchers.Main).launch {
                GlobalSettings.getShowOnLockScreen(requireContext()).collect {
                    isChecked = it
                }
            }
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    GlobalSettings.setShowOnLockScreen(requireContext(), isChecked)
                }
            }
        }

        binding.vibrationSwitch.apply {
            CoroutineScope(Dispatchers.Main).launch {
                GlobalSettings.getVibrated(requireContext()).collect {
                    isChecked = it
                }
            }
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    GlobalSettings.setVibrated(requireContext(), isChecked)
                }
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}