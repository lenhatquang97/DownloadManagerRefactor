package com.quangln2.mydownloadmanager.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase.Companion.getDatabase
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding

class HomeFragment() : Fragment() {

    private lateinit var binding: FragmentFirstBinding

    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(DefaultDownloadRepository((activity?.application as DownloadManagerApplication).database.downloadDao()),requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel

        val adapterVal = DownloadListAdapter(requireContext())

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.downloadListSchema.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty() && viewModel._downloadList.value != null && viewModel._downloadList.value?.size == 0) {
                    viewModel._downloadList.value = it.toMutableList()
                }
            }
        }

        viewModel.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    println("Is not empty")
                    adapterVal.submitList(it)
                    adapterVal.notifyItemChanged(it.size-1)
                }
            }
        }

        viewModel.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    println("Is not empty")
                    adapterVal.submitList(it)
                }
            }
        }

        binding.chip0.setOnClickListener {
            adapterVal.submitList(viewModel.downloadList.value)
        }
        binding.chip1.setOnClickListener {
            viewModel.filterList(DownloadStatusState.DOWNLOADING.toString())
        }
        binding.chip2.setOnClickListener {
            viewModel.filterList(DownloadStatusState.FAILED.toString())
        }
        binding.chip3.setOnClickListener {
            viewModel.filterList(DownloadStatusState.PAUSED.toString())
        }
        binding.chip4.setOnClickListener {
            viewModel.filterList(DownloadStatusState.COMPLETED.toString())
        }
        binding.chip5.setOnClickListener {
            viewModel.filterList(DownloadStatusState.QUEUED.toString())
        }
    }

}