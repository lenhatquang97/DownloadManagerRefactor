package com.quangln2.mydownloadmanager.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private val viewModel: HomeViewModel by activityViewModels { ViewModelFactory(DefaultDownloadRepository(), requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFirstBinding.inflate(inflater, container,false)
        binding.viewModel = viewModel

        val adapterVal = DownloadListAdapter(requireContext())
        adapterVal.submitList(viewModel._downloadList.value)

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }

        viewModel._downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    adapterVal.submitList(it)
                    adapterVal.notifyItemChanged(it.size - 1)
                    println("Listen: " + it.first().fileName)
                }

            }
        }
        binding.lifecycleOwner = this


        binding.chip1.setOnClickListener {
            if(viewModel._downloadList.value?.size!! > 0){
                println(viewModel._downloadList.value?.get(0)!!.fileName)
            }
            if(viewModel._item.value != null){
                println("HomeFragment " + viewModel._item.value?.fileName)
            }
            else{
                println("Failed")
            }

        }
        return binding.root

    }

}