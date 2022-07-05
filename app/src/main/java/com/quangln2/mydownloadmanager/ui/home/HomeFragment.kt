package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        binding.viewModel = viewModel


        val adapterVal = DownloadListAdapter(context!!)
        adapterVal.eventListener = object : EventListener{
            override fun onFirstSetup(item: StrucDownFile) {
                if(context != null){
                    item.bytesCopied = ExternalUse.getBytesFromExistingFileUseCase(context!!)(item,context!!)
                }
            }

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

            override fun downloadAFileWithProgressBar(
                binding: DownloadItemBinding,
                item: StrucDownFile,
                context: Context
            ) {
                var startTime = System.currentTimeMillis()
                var endTime: Long

                var startBytes = 0L
                var endBytes: Long

                if(item.downloadState == DownloadStatusState.PAUSED){
                    return
                }
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO){
                        onOpenNotification(item, item.convertToSizeUnit() + " - " + item.downloadState.toString(), -1)
                    }
                    binding.heading.text = LogicUtil.cutFileName(item.fileName)
                    ExternalUse.downloadAFileUseCase(context)(item, context).collect { itr ->
                        binding.progressBar.progress = itr
                        endTime = System.currentTimeMillis()
                        endBytes = item.bytesCopied
                        val seconds = ((endTime.toDouble() - startTime.toDouble()) / 1000.0)
                        if(seconds > 1){
                            val result = LogicUtil.calculateDownloadSpeed(seconds, startBytes, endBytes)
                            binding.textView.text = result + " - "+ item.convertToSizeUnit() + " - " + item.downloadState.toString()
                            withContext(Dispatchers.IO){
                                onOpenNotification(item, result + " - "+ item.convertToSizeUnit() + " - " + item.downloadState.toString(), itr)
                            }
                            startBytes = endBytes
                            startTime = endTime
                        }
                        if(itr == 100){
                            item.downloadState = DownloadStatusState.COMPLETED
                            binding.stopButton.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                            binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                            binding.downloadStateButton.setImageResource(R.drawable.ic_open)

                            withContext(Dispatchers.IO){
                                onOpenNotification(item, item.convertToSizeUnit() + " - " + item.downloadState.toString(), 100)
                                ExternalUse.updateToListUseCase(context)(item)
                                ExternalUse.howManyFileDownloading -= 1
                            }
                        }
                    }
                }
            }
        }




        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }


        viewModel.downloadListSchema.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty() && viewModel._downloadList.value != null && viewModel._downloadList.value?.size == 0) {
                    viewModel._downloadList.value = it.toMutableList()
                    viewModel._filterList.value = it.toMutableList()
                }
            }
        }

        viewModel.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    viewModel._filterList.value = it
                    adapterVal.notifyItemChanged(it.size - 1)
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
                adapterVal.submitList(it)

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