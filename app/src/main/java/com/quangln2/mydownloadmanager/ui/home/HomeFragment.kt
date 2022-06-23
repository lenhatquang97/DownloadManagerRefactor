package com.quangln2.mydownloadmanager.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ServiceLocator
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.AddDownloadDialogBinding
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding
import com.quangln2.mydownloadmanager.getViewModelFactory

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding

    private val downloadList = mutableListOf<StrucDownFile>()
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadList.addAll(listOf(ServiceLocator.initializeStrucDownFile()))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFirstBinding.inflate(inflater, container,false)

        val downloadViewModelFactory = ViewModelFactory(DefaultDownloadRepository(), context!!)
        viewModel = ViewModelProvider(this, downloadViewModelFactory).get(HomeViewModel::class.java)

        binding.viewModel = viewModel

        val adapterVal = DownloadListAdapter()
        binding.downloadLists.apply {
            viewModel = viewModel
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.downloadList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapterVal.submitList(it)
                if(!it.isEmpty()){
                    println("Listen: " + it.first().fileName)
                }

            }
        })
        binding.lifecycleOwner = this

        binding.chip1.setOnClickListener {
            if(viewModel._item.value != null){
                println(viewModel._item.value?.fileName)
            }
            else{
                println("Failed")
            }

        }




        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}