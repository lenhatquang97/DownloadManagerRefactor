package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.listener.EventListener
import com.quangln2.mydownloadmanager.util.LogicUtil
import com.quangln2.mydownloadmanager.util.LogicUtil.Companion.cutFileName
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import java.util.concurrent.Executors

class DownloadListAdapter(private var context: Context) :
    ListAdapter<StructureDownFile, DownloadListAdapter.DownloadItemViewHolder>(
        AsyncDifferConfig.Builder(DownloadListDiffCallback())
            .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
            .build()
    ) {
    var eventListener: EventListener? = null

    inner class DownloadItemViewHolder constructor(private val binding: DownloadItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var startTime = System.currentTimeMillis()
        private var endTime: Long = 0L
        private var startBytes = 0L
        private var endBytes: Long = 0L
        private fun initialSetup(item: StructureDownFile) {
            binding.heading.text = cutFileName(item.fileName)
            binding.textView.text =
                if (binding.textView.text.isNullOrEmpty()) item.convertToSizeUnit() + " - " +
                        item.downloadState.toString() else binding.textView.text.toString()
            binding.roundCategory.setImageResource(UIComponentUtil.defineIcon(item.kindOf))
            when (item.downloadState) {
                DownloadStatusState.COMPLETED -> {
                    binding.apply {
                        moreButton.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_open)

                    }
                }
                DownloadStatusState.DOWNLOADING -> {
                    binding.apply {
                        moreButton.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        stopButton.visibility = View.VISIBLE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                }
                DownloadStatusState.QUEUED -> {
                    binding.apply {
                        moreButton.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.GONE
                    }
                }
                DownloadStatusState.PAUSED -> {
                    binding.apply {
                        moreButton.visibility = View.VISIBLE
                        progressBar.visibility = View.VISIBLE
                        stopButton.visibility = View.VISIBLE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_start)
                        progressBar.progress =
                            (item.bytesCopied.toFloat() / item.size.toFloat() * 100.0).toInt()
                    }
                }
                DownloadStatusState.FAILED -> {
                    binding.apply {
                        moreButton.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        stopButton.visibility = View.GONE
                        downloadStateButton.visibility = View.VISIBLE
                        downloadStateButton.setImageResource(R.drawable.ic_retry)
                    }
                }
                else -> {}
            }
            binding.moreButton.setOnClickListener {
                val wrapper = ContextThemeWrapper(context, R.style.MoreButtonFunction_PopupMenu)
                val popup = PopupMenu(wrapper, binding.moreButton)
                popup.inflate(R.menu.viewholder_more_menu)
                popup.setOnMenuItemClickListener {
                    eventListener?.onHandleDelete(it, binding, item, context) ?: false
                }
                popup.show()
            }
        }

        fun bind(item: StructureDownFile, context: Context) {
            initialSetup(item)
            binding.downloadStateButton.setOnClickListener {
                when (item.downloadState) {
                    DownloadStatusState.DOWNLOADING -> {
                        binding.moreButton.visibility = View.VISIBLE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_start)
                        eventListener?.onPause(item)
                    }
                    DownloadStatusState.PAUSED -> {
                        binding.moreButton.visibility = View.GONE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                        binding.progressBar.progress =
                            (item.bytesCopied.toFloat() / item.size.toFloat() * 100.0).toInt()
                        eventListener?.onResume(item)
                    }
                    DownloadStatusState.COMPLETED -> {
                        binding.moreButton.visibility = View.VISIBLE
                        binding.stopButton.visibility = View.GONE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                        eventListener?.onOpen(item)
                    }
                    DownloadStatusState.FAILED -> {
                        binding.moreButton.visibility = View.VISIBLE
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.VISIBLE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                        eventListener?.onRetry(item)
                    }
                    else -> {}
                }

                binding.textView.text =
                    item.convertToSizeUnit() + " - " + item.downloadState.toString()
                eventListener?.onUpdateToDatabase(item)
            }
            binding.stopButton.setOnClickListener {
                if (item.downloadState == DownloadStatusState.DOWNLOADING || item.downloadState == DownloadStatusState.PAUSED) {
                    eventListener?.onStop(item, binding)
                }
            }

            //Calculate download speed
            if (item.downloadState == DownloadStatusState.DOWNLOADING) {
                binding.heading.text = cutFileName(item.fileName)
                binding.progressBar.progress =
                    (item.bytesCopied.toFloat() / item.size.toFloat() * 100.0).toInt()
                endTime = System.currentTimeMillis()
                endBytes = item.bytesCopied
                val seconds = ((endTime.toDouble() - startTime.toDouble()) / 1000.0)
                val result = LogicUtil.calculateDownloadSpeed(seconds, startBytes, endBytes)
                if (seconds > 0.8 && result > 0) {
                    binding.textView.text = String.format(
                        "%.2f MB/s",
                        result
                    ) + " - " + item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    startBytes = endBytes
                    startTime = endTime
                }
            }


            if (item.downloadState == DownloadStatusState.FAILED) {
                eventListener?.onStop(item, binding)
                eventListener?.onUpdateToDatabase(item)

            }

            if (item.bytesCopied == item.size && item.downloadState != DownloadStatusState.FAILED) {
                item.downloadState = DownloadStatusState.COMPLETED
                binding.moreButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.textView.text =
                    item.convertToSizeUnit() + " - " + item.downloadState.toString()
                binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                eventListener?.onDownloadSuccess(binding, item, context)
            }
        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadListAdapter.DownloadItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DownloadItemBinding.inflate(layoutInflater, parent, false)
        return this.DownloadItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context)
    }

    fun updateProgress(file: StructureDownFile) {
        val index = currentList.indexOfFirst { it.id == file.id }
        val mutableList = currentList.toMutableList()
        if (index == -1) {
            return
        }
        mutableList[index] = file
        submitList(mutableList.toMutableList())
        notifyItemChanged(index)
    }

    override fun getItemId(position: Int): Long {
        val item = currentList[position]
        return item.id.hashCode().toLong()
    }


}

class DownloadListDiffCallback : DiffUtil.ItemCallback<StructureDownFile>() {
    override fun areItemsTheSame(oldItem: StructureDownFile, newItem: StructureDownFile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: StructureDownFile,
        newItem: StructureDownFile
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.downloadState == newItem.downloadState
                && oldItem.fileName == newItem.fileName && oldItem.size == newItem.size
                && oldItem.bytesCopied == newItem.bytesCopied
    }
}
