package com.quangln2.downloadmanagerrefactor.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.quangln2.downloadmanagerrefactor.data.model.settings.GlobalSettings
import com.quangln2.downloadmanagerrefactor.databinding.FragmentSecondBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        binding.downloadThreadSlider.apply {
            value = GlobalSettings.numsOfMaxDownloadThreadExported.toFloat()
            addOnChangeListener { _, value, _ ->
                GlobalSettings.numsOfMaxDownloadThreadExported = value.toInt()
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