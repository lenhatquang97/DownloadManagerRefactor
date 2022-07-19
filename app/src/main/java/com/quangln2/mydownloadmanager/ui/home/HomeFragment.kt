package com.quangln2.mydownloadmanager.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.progressFile
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.downloadList
import com.quangln2.mydownloadmanager.data.source.local.LocalDataSourceImpl
import com.quangln2.mydownloadmanager.data.source.remote.RemoteDataSourceImpl
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.service.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding

    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(
            DefaultDownloadRepository(
                DownloadManagerApplication.database.downloadDao(),
                LocalDataSourceImpl(),
                RemoteDataSourceImpl()
            )
        )
    }

    var downloadService: DownloadService? = null
    var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as DownloadService.MyLocalBinder
            downloadService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(requireContext(), DownloadService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterVal = DownloadListAdapter(requireContext())
        adapterVal.eventListener = object : EventListener {

            override fun onHandleDelete(
                menuItem: MenuItem,
                binding: DownloadItemBinding,
                item: StrucDownFile,
                context: Context
            ): Boolean {
                return when (menuItem.itemId) {
                    R.id.delete_from_list_option -> {
                        viewModel.deleteFromList(item)
                        binding.textView.text = ""
                        true
                    }
                    R.id.delete_permanently_option -> {
                        val onHandle = fun(flag: Boolean) {
                            if (flag) {
                                binding.textView.text = ""
                            }
                        }
                        viewModel.deletePermanently(context, item, onHandle)
                        true
                    }
                    else -> false
                }
            }

            override fun onDownloadSuccess(
                binding: DownloadItemBinding,
                item: StrucDownFile,
                context: Context
            ) {
                item.downloadState = DownloadStatusState.COMPLETED
                binding.stopButton.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.textView.text =
                    item.convertToSizeUnit() + " - " + item.downloadState.toString()
                binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                CoroutineScope(Dispatchers.IO).launch {
                    DownloadManagerApplication.downloadRepository.update(item)
                    withContext(Dispatchers.Main) {
                        GlobalSettings.getVibrated(context).collect {
                            if (it) viewModel.vibratePhone(context)
                        }
                    }
                }
            }

            override fun onPause(item: StrucDownFile) {
                viewModel.pause(item.id)
                DownloadManagerController.howManyFileDownloadingParallel--
            }

            override fun onResume(item: StrucDownFile) {
                viewModel.resume(requireContext(), item.id)
                DownloadManagerController.howManyFileDownloadingParallel++
            }

            override fun onOpen(item: StrucDownFile) {
                viewModel.open(requireContext(), item)
            }

            override fun onRetry(item: StrucDownFile) {
                viewModel.retry(requireContext(), item)
                DownloadManagerController.howManyFileDownloadingParallel++
            }

            override fun onStop(item: StrucDownFile) {
                DownloadManagerController.howManyFileDownloadingParallel--
            }

            override fun onUpdateToDatabase(item: StrucDownFile) {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.update(item)
                }
            }
        }

        val animator: ItemAnimator = binding.downloadLists.itemAnimator!! // your recycler view here
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(context)
        }

        DownloadManagerController.downloadListSchema?.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty() && DownloadManagerController._downloadList.value != null &&
                    DownloadManagerController._downloadList.value?.size == 0) {
                    DownloadManagerController._downloadList.value = it.toMutableList()
                }
            }

        }



        downloadList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    binding.chip0.performClick()
                    viewModel._filterList.value = it
                }
            }
        }

        viewModel.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    binding.downloadLists.visibility = View.INVISIBLE
                    binding.emptyDataParent.visibility = View.VISIBLE
                } else {
                    binding.downloadLists.visibility = View.VISIBLE
                    binding.emptyDataParent.visibility = View.GONE
                    if(viewModel.textSearch.value != null){
                        val result = it.filter { itr -> itr.fileName.lowercase().contains(
                            viewModel.textSearch.value!!.lowercase()) }
                        adapterVal.submitList(result.toMutableList())
                    }

                }
                return@observe

            }
        }


        progressFile.observe(viewLifecycleOwner) {
            it?.let {
                if (it.size != -1L) {
                    adapterVal.updateProgress(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        val progress = (it.bytesCopied.toFloat() / it.size.toFloat() * 100).toInt()
                        if (progress == 100) {
                            it.downloadState = DownloadStatusState.COMPLETED
                            DownloadManagerApplication.downloadRepository.update(it)
                        }
                    }
                }
            }
        }

        binding.searchField.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterStartsWithNameCaseInsensitive(s.toString())
                viewModel.textSearch.value = s.toString()
            }
        })

        binding.stateGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.chip0 -> viewModel.filterList(DownloadStatusState.ALL.toString())
                R.id.chip1 -> viewModel.filterList(DownloadStatusState.DOWNLOADING.toString())
                R.id.chip2 -> viewModel.filterList(DownloadStatusState.FAILED.toString())
                R.id.chip3 -> viewModel.filterList(DownloadStatusState.PAUSED.toString())
                R.id.chip4 -> viewModel.filterList(DownloadStatusState.COMPLETED.toString())
                R.id.chip5 -> viewModel.filterList(DownloadStatusState.QUEUED.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            requireContext().unbindService(connection)
            val currentList = DownloadManagerController._downloadList.value
            val res = currentList?.find { it.downloadState == DownloadStatusState.DOWNLOADING }
            if(res != null) {
                requireContext().stopService(Intent(requireContext(), DownloadService::class.java))
            }
            isBound = false
        }
    }
}