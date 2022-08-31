package com.quangln2.downloadmanagerrefactor.ui.home

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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.quangln2.downloadmanagerrefactor.DownloadManagerApplication
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.ViewModelFactory
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController._downloadList
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.downloadList
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.progressFile
import com.quangln2.downloadmanagerrefactor.data.database.DownloadDao
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.data.model.settings.GlobalSettings
import com.quangln2.downloadmanagerrefactor.data.repository.DefaultDownloadRepository
import com.quangln2.downloadmanagerrefactor.data.source.local.LocalDataSourceImpl
import com.quangln2.downloadmanagerrefactor.data.source.remote.RemoteDataSourceImpl
import com.quangln2.downloadmanagerrefactor.databinding.DownloadItemBinding
import com.quangln2.downloadmanagerrefactor.databinding.FragmentFirstBinding
import com.quangln2.downloadmanagerrefactor.listener.EventListener
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var adapterVal: DownloadListAdapter

    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(
            DefaultDownloadRepository(
                LocalDataSourceImpl(DownloadDao()),
                RemoteDataSourceImpl(),
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

        adapterVal = DownloadListAdapter(requireContext())
        adapterVal.eventListener = object : EventListener {

            override fun onHandleDelete(
                menuItem: MenuItem,
                binding: DownloadItemBinding,
                item: StructureDownFile,
                context: Context
            ): Boolean {
                return when (menuItem.itemId) {
                    R.id.delete_from_list_option -> {
                        viewModel.deleteFromList(item, context)
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
                item: StructureDownFile,
                context: Context
            ) {
                viewModel.update(item.copy(downloadState = DownloadStatusState.COMPLETED))
                lifecycleScope.launch {
                    GlobalSettings.getVibrated(context).collect {
                        if (it) viewModel.vibratePhone(context)
                    }
                }
            }

            override fun onPause(item: StructureDownFile) = viewModel.pause(item)
            override fun onResume(item: StructureDownFile) = viewModel.resume(item, requireContext())
            override fun onRetry(item: StructureDownFile) = viewModel.retry(requireContext(), item)
            override fun onStop(item: StructureDownFile, binding: DownloadItemBinding, context: Context) =
                viewModel.stop(item, context)

            override fun onOpen(item: StructureDownFile) = viewModel.open(requireContext(), item)
            override fun onUpdateToDatabase(item: StructureDownFile) = viewModel.update(item)
        }

        val animator: ItemAnimator = binding.downloadLists.itemAnimator!!
        if (animator is DefaultItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        binding.downloadLists.apply {
            adapter = adapterVal
            layoutManager = LinearLayoutManager(requireContext())
        }

        DownloadDao().getAll().let {
                if (it.isNotEmpty() && _downloadList.value != null &&
                    _downloadList.value?.size == 0
                ) {
                    val currentList = it
                    for (item in currentList) {
                        if (item.downloadState == DownloadStatusState.DOWNLOADING) {
                            item.downloadState = DownloadStatusState.PAUSED
                        }
                    }
                    _downloadList.postValue(it.toMutableList())
                }
        }

        downloadList.observe(viewLifecycleOwner) {
            it?.let {
                DownloadManagerController._filterList.value = it.toMutableList()
            }
        }

        DownloadManagerController.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isEmpty()) {
                    binding.downloadLists.visibility = View.INVISIBLE
                    binding.emptyDataParent.visibility = View.VISIBLE
                } else {
                    binding.downloadLists.visibility = View.VISIBLE
                    binding.emptyDataParent.visibility = View.GONE
                    if (viewModel.textSearch.value != null) {
                        val result = it.filter { itr ->
                            itr.fileName.lowercase().contains(
                                viewModel.textSearch.value!!.lowercase()
                            )
                        }
                        adapterVal.submitList(result.toMutableList())
                    }
                }

            }
        }

        progressFile.observe(viewLifecycleOwner) {
            it?.let {
                if(DownloadManagerController.filterName == "All") {
                    val visibleChild =
                        binding.downloadLists.getChildAt(
                            DownloadManagerController.filterList.value?.size?.minus(1) ?: 0
                        )
                    val lastChild = binding.downloadLists.getChildAdapterPosition(visibleChild)
                    if (lastChild == DownloadManagerController.filterList.value?.size?.minus(1)) {
                        adapterVal.updateProgress(it)
                    }
                } else {
                    adapterVal.updateProgress(it)
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    val progress = (it.bytesCopied.toFloat() / it.size.toFloat() * 100).toInt()
                    if (progress == 100) {
                        it.downloadState = DownloadStatusState.COMPLETED
                        viewModel.update(it)
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
            when (checkedId) {
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
            val currentList = _downloadList.value
            val res = currentList?.find { it.downloadState == DownloadStatusState.DOWNLOADING }
            if (res == null) {
                requireContext().stopService(Intent(requireContext(), DownloadService::class.java))
                requireContext().unbindService(connection)
                isBound = false
            }
        }
        _downloadList.value?.forEach {
            if (it.downloadState == DownloadStatusState.COMPLETED && context != null) {
                deleteTempFile(it, context!!)
            }
        }
    }

    private fun deleteTempFile(file: StructureDownFile, context: Context) {
        (0 until DownloadManagerController.numberOfChunks).forEach {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.chunkNames[it])
            if (appSpecificExternalDir.exists()) {
                appSpecificExternalDir.delete()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        detectFileFailed()
    }

    private fun detectFileFailed() {
        val res = downloadList.value
        val indexArray = mutableListOf<Int>()
        if (res != null) {
            for (i in 0 until res.size) {
                if (!DownloadUtil.isFileExisting(
                        res[i],
                        requireContext()
                    ) && res[i].downloadState == DownloadStatusState.COMPLETED
                ) {
                    res[i].downloadState = DownloadStatusState.FAILED
                    indexArray.add(i)
                }
            }
            if (indexArray.size > 0) {
                _downloadList.postValue(res.toMutableList())
                adapterVal.submitList(res.toMutableList())
                for (i in indexArray) {
                    adapterVal.notifyItemChanged(i)
                }

            }
        }
    }
}