package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.pauseDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.resumeDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.retryDownloadUseCase
import com.quangln2.mydownloadmanager.util.LogicUtil
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
        var startTime = System.currentTimeMillis()
        var endTime: Long = 0L
        var startBytes = 0L
        var endBytes: Long = 0L
        private fun initialSetup(item: StrucDownFile){
            binding.heading.text = cutFileName(item.fileName)
            binding.textView.text = if(binding.textView.text.isNullOrEmpty()) item.convertToSizeUnit() + " - " + item.downloadState.toString() else binding.textView.text.toString()
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
                else -> {

                }
            }

        }
        fun bind(item: StrucDownFile, context: Context){
            initialSetup(item)
            binding.downloadStateButton.setOnClickListener {
                when(item.downloadState){
                    DownloadStatusState.DOWNLOADING -> {
                        DownloadManagerController.pause(item.id)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_start)
                    }
                    DownloadStatusState.PAUSED -> {
                        DownloadManagerController.resume(context, item.id)
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
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    else -> {

                    }
                }
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
            }
            binding.stopButton.setOnClickListener {
                if(item.downloadState == DownloadStatusState.DOWNLOADING || item.downloadState == DownloadStatusState.PAUSED){
                    item.downloadState = DownloadStatusState.FAILED
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    binding.downloadStateButton.setImageResource(R.drawable.ic_retry)
                }
            }
            println("Download State " + item.downloadState.toString())
            if(item.downloadState == DownloadStatusState.DOWNLOADING){
                binding.heading.text = cutFileName(item.fileName)
                binding.progressBar.progress = (item.bytesCopied.toFloat() / item.size.toFloat() * 100.0).toInt()
                endTime = System.currentTimeMillis()
                endBytes = item.bytesCopied
                val seconds = ((endTime.toDouble() - startTime.toDouble()) / 1000.0)
                if(seconds > 1){
                    println(seconds)
                    val result = LogicUtil.calculateDownloadSpeed(seconds, startBytes, endBytes)
                    binding.textView.text = result + " - " + item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    startBytes = endBytes
                    startTime = endTime
                }
            }

            if (binding.progressBar.progress == 100) {
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
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadListAdapter.DownloadItemViewHolder {
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

    fun updateProgress(file: StrucDownFile) {
        val index = currentList.indexOfFirst {it.id == file.id }
        val mutableList = currentList.toMutableList()
        if(index == -1){
            return
        }
        mutableList[index] = file
        submitList(mutableList)
        notifyItemChanged(index)

    }


}
class DownloadListDiffCallback: DiffUtil.ItemCallback<StrucDownFile>() {
    override fun areItemsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem.id == newItem.id && oldItem.downloadState == newItem.downloadState
                && oldItem.fileName == newItem.fileName && oldItem.size == newItem.size
                && oldItem.bytesCopied == newItem.bytesCopied
    }
}
