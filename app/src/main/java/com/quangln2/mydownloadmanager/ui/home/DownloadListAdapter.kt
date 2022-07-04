package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.pauseDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.resumeDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.retryDownloadUseCase
import com.quangln2.mydownloadmanager.util.LogicUtil.Companion.cutFileName
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class DownloadListAdapter(private var context: Context): ListAdapter<StrucDownFile, DownloadListAdapter.DownloadItemViewHolder>(
    AsyncDifferConfig.Builder(DownloadListDiffCallback()).setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {
    var eventListener: EventListener? = null

    inner class DownloadItemViewHolder constructor(private val binding: DownloadItemBinding): RecyclerView.ViewHolder(binding.root){
        private fun initialSetup(item: StrucDownFile){
            binding.heading.text = cutFileName(item.fileName)
            binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
            binding.progressBar.progress = 0
            binding.roundCategory.setImageResource(UIComponentUtil.defineIcon(item.kindOf))
            when (item.downloadState) {
                DownloadStatusState.COMPLETED -> {
                    binding.apply {
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_open)
                    }
                }
                DownloadStatusState.DOWNLOADING -> {
                    binding.apply {
                        progressBar.visibility = View.VISIBLE
                        stopButton.visibility = View.VISIBLE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                }
                DownloadStatusState.QUEUED -> {
                    binding.apply {
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.GONE
                    }
                }
                DownloadStatusState.PAUSED -> {
                    binding.apply {
                        progressBar.visibility = View.VISIBLE
                        stopButton.visibility = View.VISIBLE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_start)
                    }
                }
                DownloadStatusState.FAILED -> {
                    binding.apply {
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_retry)
                    }
                }
            }

        }
        fun bind(item: StrucDownFile, context: Context){
            initialSetup(item)
            eventListener?.onFirstSetup(item)
            binding.downloadStateButton.setOnClickListener {
                when(item.downloadState){
                    DownloadStatusState.DOWNLOADING -> {
                        pauseDownloadUseCase(context)(item)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_start)
                    }
                    DownloadStatusState.PAUSED -> {
                        resumeDownloadUseCase(context)(item)
                        eventListener?.downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    DownloadStatusState.COMPLETED -> {
                        item.downloadState = DownloadStatusState.COMPLETED
                        binding.stopButton.visibility = View.GONE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                        ExternalUse.openDownloadFileUseCase(context)(item,context)
                    }
                    DownloadStatusState.FAILED -> {
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.VISIBLE
                        retryDownloadUseCase(context)(item, context)
                        eventListener?.downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    else -> {

                    }
                }
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    eventListener?.onOpenNotification(item, binding.textView.text as String, -1)
                    ExternalUse.updateToListUseCase(context)(item)
                }
            }


            binding.stopButton.setOnClickListener {
                if(item.downloadState == DownloadStatusState.DOWNLOADING || item.downloadState == DownloadStatusState.PAUSED){
                    item.downloadState = DownloadStatusState.FAILED
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    binding.downloadStateButton.setImageResource(R.drawable.ic_retry)

                    CoroutineScope(Dispatchers.IO).launch {
                        eventListener?.onOpenNotification(item, binding.textView.text as String, -1)
                        ExternalUse.updateToListUseCase(context)(item)
                    }
                }
            }
            if(item.downloadState == DownloadStatusState.DOWNLOADING){
                eventListener?.downloadAFileWithProgressBar(binding, item, context)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DownloadItemBinding.inflate(layoutInflater, parent, false)
        return this.DownloadItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context)
    }

    override fun submitList(list: MutableList<StrucDownFile>?) {
        super.submitList(list ?: mutableListOf())
    }
}
class DownloadListDiffCallback: DiffUtil.ItemCallback<StrucDownFile>() {
    override fun areItemsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem == newItem
    }
}