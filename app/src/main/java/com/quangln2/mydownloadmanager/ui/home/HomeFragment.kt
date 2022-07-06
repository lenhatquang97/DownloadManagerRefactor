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
import com.quangln2.mydownloadmanager.listener.ProgressCallback
import com.quangln2.mydownloadmanager.service.DownloadService
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.util.LogicUtil

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
            ) {}
        }

        (binding.downloadLists.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false



        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }





        DownloadManagerController.downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    DownloadManagerController._filterList.value = it
                    adapterVal.notifyItemInserted(it.size - 1)
                }
            }
        }

        DownloadManagerController.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isEmpty()){
                    binding.downloadLists.visibility = View.INVISIBLE
                    return@observe
                }
                binding.downloadLists.visibility = View.VISIBLE
                adapterVal.submitList(it)

            }
        }

        DownloadManagerController.progressFile.observe(viewLifecycleOwner){
            it?.let {
                if(it != null && it.size != -1L){
                    adapterVal.updateProgress(it)
                }

            }
        }


        binding.searchField.editText?.addTextChangedListener( object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    DownloadManagerController.filterStartsWithNameCaseInsensitive(s.toString())
                }
            })

        binding.chip0.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.ALL.toString())
        }
        binding.chip1.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.DOWNLOADING.toString())
        }
        binding.chip2.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.FAILED.toString())
        }
        binding.chip3.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.PAUSED.toString())
        }
        binding.chip4.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.COMPLETED.toString())
        }
        binding.chip5.setOnClickListener {
            DownloadManagerController.filterList(DownloadStatusState.QUEUED.toString())
        }
    }
}