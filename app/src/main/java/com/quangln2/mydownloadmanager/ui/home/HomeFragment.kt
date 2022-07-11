package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.service.DownloadService
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.util.LogicUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

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


        val adapterVal = DownloadListAdapter(context!!)
        adapterVal.eventListener = object : EventListener{
            override fun onOpenNotification(item: StrucDownFile, content: String, progress: Int) {
                val intent = Intent(context, DownloadService::class.java)
                intent.putExtra("fileName", LogicUtil.cutFileName(item.fileName))
                intent.putExtra("content", content)
                intent.putExtra("id", item.id.hashCode())
                if(progress != -1){
                    intent.putExtra("progress", progress)
                }
                requireContext().startForegroundService(intent)
            }
            override fun onHandleDelete(menuItem: MenuItem, binding: DownloadItemBinding, item: StrucDownFile, context: Context): Boolean{
                return when(menuItem.itemId){
                    R.id.delete_from_list_option -> {
                        viewModel.deleteFromList(context, item)
                        binding.textView.text = ""
                        true
                    }
                    R.id.delete_permanently_option -> {
                        val onHandle = fun(flag: Boolean) {
                            if(flag){ binding.textView.text = "" }
                        }
                        viewModel.deletePermanently(context, item, onHandle)
                        true
                    }
                    else -> false
                }
            }
            override fun onDownloadSuccess(binding: DownloadItemBinding, item: StrucDownFile, context: Context) {
                item.downloadState = DownloadStatusState.COMPLETED
                binding.stopButton.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                CoroutineScope(Dispatchers.IO).launch {
                    ExternalUse.updateToListUseCase(context)(item)
                    ExternalUse.howManyFileDownloading -= 1
                }
            }
        }

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }
        (binding.downloadLists.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        DownloadManagerController.downloadListSchema?.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty() && DownloadManagerController._downloadList.value != null && DownloadManagerController._downloadList.value?.size == 0) {
                    DownloadManagerController._downloadList.value = it.toMutableList()
                }
            }

        }



        DownloadManagerController.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.downloadLists.visibility = View.VISIBLE
                    binding.emptyDataParent.visibility = View.GONE
                    viewModel._filterList.value = it
                } else {
                    binding.emptyDataParent.visibility = View.VISIBLE
                    binding.downloadLists.visibility = View.GONE
                }
            }
        }

        viewModel.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isEmpty()){
                    binding.downloadLists.visibility = View.INVISIBLE
                    return@observe
                }
                binding.downloadLists.visibility = View.VISIBLE
                adapterVal.submitList(it.toMutableList())
                //To handle case that recyclerview does not rendering again
                if(binding.chip0.chipIcon != null){
                    viewModel.filterList(DownloadStatusState.ALL.toString())
                }
            }
        }

        DownloadManagerController.progressFile.observe(viewLifecycleOwner){
            it?.let {
                if(it.size != -1L){
                    adapterVal.updateProgress(it)
                }

            }
        }

        binding.searchField.editText?.addTextChangedListener( object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.filterStartsWithNameCaseInsensitive(s.toString())
                }
            })
        binding.chip0.setOnClickListener {
            viewModel.filterList(DownloadStatusState.ALL.toString())

            binding.chip0.chipIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_check_circle_outline_24)
            binding.chip1.chipIcon = null
            binding.chip2.chipIcon = null
            binding.chip3.chipIcon = null
            binding.chip4.chipIcon = null
            binding.chip5.chipIcon = null
        }
        binding.chip1.setOnClickListener {
            viewModel.filterList(DownloadStatusState.DOWNLOADING.toString())

            binding.chip0.chipIcon = null
            binding.chip1.chipIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_check_circle_outline_24)
            binding.chip2.chipIcon = null
            binding.chip3.chipIcon = null
            binding.chip4.chipIcon = null
            binding.chip5.chipIcon = null
        }
        binding.chip2.setOnClickListener {
            viewModel.filterList(DownloadStatusState.FAILED.toString())

            binding.chip0.chipIcon = null
            binding.chip1.chipIcon = null
            binding.chip2.chipIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_check_circle_outline_24)
            binding.chip3.chipIcon = null
            binding.chip4.chipIcon = null
            binding.chip5.chipIcon = null
        }
        binding.chip3.setOnClickListener {
            viewModel.filterList(DownloadStatusState.PAUSED.toString())

            binding.chip0.chipIcon = null
            binding.chip1.chipIcon = null
            binding.chip2.chipIcon = null
            binding.chip3.chipIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_check_circle_outline_24)
            binding.chip4.chipIcon = null
            binding.chip5.chipIcon = null


        }
        binding.chip4.setOnClickListener {
            viewModel.filterList(DownloadStatusState.COMPLETED.toString())

            binding.chip0.chipIcon = null
            binding.chip1.chipIcon = null
            binding.chip2.chipIcon = null
            binding.chip3.chipIcon = null
            binding.chip4.chipIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_baseline_check_circle_outline_24)
            binding.chip5.chipIcon = null


        }
        binding.chip5.setOnClickListener {
            viewModel.filterList(DownloadStatusState.QUEUED.toString())

            binding.chip0.chipIcon = null
            binding.chip1.chipIcon = null
            binding.chip2.chipIcon = null
            binding.chip3.chipIcon = null
            binding.chip4.chipIcon = null
            binding.chip5.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_check_circle_outline_24)


        }
    }
}