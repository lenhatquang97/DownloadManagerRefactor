package com.quangln2.mydownloadmanager.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
private const val TAG = "SettingsFragment"
class SettingsFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.popUpMessagesSwitch.apply {
            isChecked = GlobalSettings.getPopUpMessage(requireContext()).value!!
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    GlobalSettings.setPopUpMessage(requireContext(), isChecked)
                }
            }
        }

        binding.showValueSwitch.apply {
            isChecked = GlobalSettings.getShowOnLockScreen(requireContext()).value!!
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    GlobalSettings.setShowOnLockScreen(requireContext(), isChecked)
                }
            }
        }

        binding.vibrationSwitch.apply {
            isChecked = GlobalSettings.getVibrated(requireContext()).value!!
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
    ): View {
        Log.d(TAG, "onCreateView: ")
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}