package com.quangln2.mydownloadmanager.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.data.database.DownloadDao
import com.quangln2.mydownloadmanager.data.database.DownloadDatabase.Companion.getDatabase
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding

class HomeFragment() : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var dao: DownloadDao
//    private val viewModel: HomeViewModel by activityViewModels {
//        ViewModelFactory(DefaultDownloadRepository(DownloadManagerApplication().database.downloadDao()),activity?.applicationContext!!)
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = getDatabase(requireContext()).downloadDao()
        viewModel = activity?.run{
            ViewModelFactory(DefaultDownloadRepository(dao),requireContext()).create(HomeViewModel::class.java)
        }!!

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

        viewModel.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    adapterVal.submitList(it)
                    adapterVal.notifyItemChanged(it.size - 1)
                }

            }
        }
    }

}