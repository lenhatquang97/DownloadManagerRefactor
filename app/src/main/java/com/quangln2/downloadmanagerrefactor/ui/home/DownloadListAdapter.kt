package com.quangln2.downloadmanagerrefactor.ui.home

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
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.databinding.DownloadItemBinding
import com.quangln2.downloadmanagerrefactor.listener.EventListener
import com.quangln2.downloadmanagerrefactor.util.LogicUtil.Companion.cutFileName
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
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

        private fun initialSetup(item: StructureDownFile) {
            binding.heading.text = cutFileName(item.fileName)
            binding.textView.text = if (binding.textView.text.isNullOrEmpty()) item.convertToSizeUnit() + " - " +
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
                        binding.progressBar.visibility = View.VISIBLE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                        eventListener?.onRetry(item)
                    }
                    else -> {}
                }

                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                eventListener?.onUpdateToDatabase(item)
            }
            binding.stopButton.setOnClickListener {
                if (item.downloadState == DownloadStatusState.DOWNLOADING || item.downloadState == DownloadStatusState.PAUSED) {
                    binding.moreButton.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.stopButton.visibility = View.GONE
                    binding.downloadStateButton.setImageResource(R.drawable.ic_retry)
                    binding.textView.text = item.convertToSizeUnit() + " - " + DownloadStatusState.FAILED.toString()
                    eventListener?.onStop(item, binding, context)
                }
            }


            if (item.downloadState == DownloadStatusState.DOWNLOADING) {
                binding.progressBar.visibility = View.VISIBLE
                binding.heading.text = cutFileName(item.fileName)
                binding.progressBar.percentArr = item.listChunks.map { l ->
                    (l.curr - l.from).toDouble() / (l.to - l.from).toDouble()
                }
                binding.textView.text = item.textProgressFormat
            }


            if (item.downloadState == DownloadStatusState.FAILED) {
                eventListener?.onStop(item, binding, context)
                eventListener?.onUpdateToDatabase(item)

            }
            if (item.bytesCopied == item.size && item.downloadState != DownloadStatusState.FAILED) {
                binding.moreButton.visibility = View.VISIBLE
                binding.stopButton.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.textView.text = item.convertToSizeUnit() + " - " + DownloadStatusState.COMPLETED.toString()
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
}

class DownloadListDiffCallback : DiffUtil.ItemCallback<StructureDownFile>() {
    override fun areItemsTheSame(oldItem: StructureDownFile, newItem: StructureDownFile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: StructureDownFile,
        newItem: StructureDownFile
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.fileName == newItem.fileName && oldItem.downloadLink == newItem.downloadLink
                && oldItem.listChunks == newItem.listChunks
    }

}
