package com.quangln2.mydownloadmanager.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.ViewModelFactory
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.data.datasource.LocalDataSourceImpl
import com.quangln2.mydownloadmanager.data.datasource.RemoteDataSourceImpl
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.data.repository.DefaultDownloadRepository
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.databinding.FragmentFirstBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.service.DownloadService
import com.quangln2.mydownloadmanager.util.LogicUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding

    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(DefaultDownloadRepository(DownloadManagerApplication.database.downloadDao(), LocalDataSourceImpl(), RemoteDataSourceImpl()))
    }

    var downloadService: DownloadService? = null
    var isBound = false
    private val connection = object : ServiceConnection{
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
        binding = FragmentFirstBinding.inflate(inflater, container,false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterVal = DownloadListAdapter(context!!)
        adapterVal.eventListener = object : EventListener{

            override fun onHandleDelete(menuItem: MenuItem, binding: DownloadItemBinding, item: StrucDownFile, context: Context): Boolean{
                return when(menuItem.itemId){
                    R.id.delete_from_list_option -> {
                        viewModel.deleteFromList(item)
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
                    DownloadManagerApplication.downloadRepository.update(item)
                    onOpenNotification(item)
                }
                val isVibrated = GlobalSettings.getVibrated(context).value
                if(isVibrated != null && isVibrated == true){
                    viewModel.vibratePhone(context)
                }
            }

            override fun onPause() {
                viewModel.pause(DownloadManagerController.progressFile.value?.id!!)
            }

            override fun onResume() {
                viewModel.resume(requireContext(), DownloadManagerController.progressFile.value?.id!!)
            }

            override fun onOpen() {
                viewModel.open(requireContext(), DownloadManagerController.progressFile.value!!)
            }

            override fun onRetry() {
                viewModel.retry(requireContext(), DownloadManagerController.progressFile.value!!)
            }

            override fun onUpdateToDatabase() {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.update(DownloadManagerController.progressFile.value!!)
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
//        (binding.downloadLists.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

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
                    binding.chip0.performClick()
                    viewModel._filterList.value = it
                }
            }
        }

        viewModel.filterList.observe(viewLifecycleOwner) {
            it?.let {
                if(it.isEmpty()){
                    binding.downloadLists.visibility = View.INVISIBLE
                    binding.emptyDataParent.visibility = View.VISIBLE
                } else {
                    binding.downloadLists.visibility = View.VISIBLE
                    binding.emptyDataParent.visibility = View.GONE
                    adapterVal.submitList(it.toMutableList())
                }
                return@observe

            }
        }

        DownloadManagerController.progressFile.observe(viewLifecycleOwner){
            it?.let {
                if(it.size != -1L){
                    adapterVal.updateProgress(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        val progress = (it.bytesCopied.toFloat() / it.size.toFloat() * 100).toInt()
                        println(progress)
                        if(progress == 100) {
                            it.downloadState = DownloadStatusState.COMPLETED
                            DownloadManagerApplication.downloadRepository.update(it)
                        }
                        onOpenNotification(it)
                    }
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
    private fun onOpenNotification(item: StrucDownFile) {
        val intent = Intent(context, DownloadService::class.java)
        val progress = (item.bytesCopied.toFloat() / item.size.toFloat() * 100).toInt()

        intent.putExtra("fileName", LogicUtil.cutFileName(item.fileName))
        intent.putExtra("content", item.downloadState.toString())
        intent.putExtra("id", item.id.hashCode())
        if(progress != -1 && progress != 100) intent.putExtra("progress", progress)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }
    override fun onStop() {
        super.onStop()
        if (isBound) {
            requireContext().unbindService(connection)
            isBound = false
        }
    }

}