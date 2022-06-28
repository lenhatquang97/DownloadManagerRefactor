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
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding

class HomeFragment() : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private val viewModel: HomeViewModel by activityViewModels { ViewModelFactory(DefaultDownloadRepository(DownloadManagerApplication().database.downloadDao()), requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFirstBinding.inflate(inflater, container,false)
        binding.viewModel = viewModel

        val adapterVal = DownloadListAdapter(requireContext())

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }


        viewModel.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    adapterVal.submitList(it)
                    adapterVal.notifyItemChanged(it.size - 1)
                }

            }
        }
        binding.lifecycleOwner = this
        return binding.root

    }

}