package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.notification.DownloadNotification
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.downloadAFileUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.pauseDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.resumeDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.retryDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.writeToFileAPI29AboveUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.writeToFileAPI29BelowUseCase
import com.quangln2.mydownloadmanager.util.LogicUtil
import com.quangln2.mydownloadmanager.util.LogicUtil.Companion.cutFileName
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadListAdapter(private var context: Context): ListAdapter<StrucDownFile, DownloadListAdapter.DownloadItemViewHolder>(DownloadListDiffCallback()) {
    class DownloadItemViewHolder private constructor(private val binding: DownloadItemBinding): RecyclerView.ViewHolder(binding.root){
        private lateinit var notification: DownloadNotification
        fun bind(item: StrucDownFile, context: Context){
            binding.heading.text = cutFileName(item.fileName)
            binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.VISIBLE
            binding.roundCategory.setImageResource(UIComponentUtil.defineIcon(item.kindOf))

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                writeToFileAPI29AboveUseCase(context)(item, context)
            } else {
                writeToFileAPI29BelowUseCase(context)(item)
            }
            notification = DownloadNotification(context, item)

            binding.downloadStateButton.setOnClickListener {
                when(item.downloadState){
                    DownloadStatusState.DOWNLOADING -> {
                        pauseDownloadUseCase(context)(item)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_start)
                    }
                    DownloadStatusState.PAUSED -> {
                        resumeDownloadUseCase(context)(item)
                        downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    DownloadStatusState.COMPLETED -> {
                        item.downloadState = DownloadStatusState.COMPLETED
                        binding.stopButton.visibility = View.GONE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                    }
                    DownloadStatusState.FAILED -> {
                        item.downloadState = DownloadStatusState.DOWNLOADING
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.VISIBLE
                        retryDownloadUseCase(context)(item, context)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            writeToFileAPI29AboveUseCase(context)(item, context)
                        } else {
                            writeToFileAPI29BelowUseCase(context)(item)
                        }
                        downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    else -> {

                    }
                }
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    notification.builder.setContentText(binding.textView.text)
                    notification.showNotification(context, item)
                }
            }
            binding.stopButton.setOnClickListener {
                if(item.downloadState == DownloadStatusState.DOWNLOADING || item.downloadState == DownloadStatusState.PAUSED){
                    item.downloadState = DownloadStatusState.FAILED
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    binding.downloadStateButton.setImageResource(R.drawable.ic_retry)

                    CoroutineScope(Dispatchers.IO).launch {
                        notification.builder.setContentText(item.convertToSizeUnit() + " - " + item.downloadState.toString())
                        notification.showNotification(context, item)
                    }


                }
            }
            if(item.downloadState != DownloadStatusState.COMPLETED){
                downloadAFileWithProgressBar(binding, item, context)
            }



        }
        companion object{
            fun from(parent: ViewGroup): DownloadItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DownloadItemBinding.inflate(layoutInflater, parent, false)
                return DownloadItemViewHolder(binding)
            }
        }


        private fun downloadAFileWithProgressBar(binding: DownloadItemBinding, item: StrucDownFile, context: Context){
            var startTime = System.currentTimeMillis()
            var endTime: Long

            var startBytes = 0L
            var endBytes: Long

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO){
                    notification.builder.setContentTitle(cutFileName(item.fileName))
                    notification.showNotification(context, item)
                }
                binding.heading.text = cutFileName(item.fileName)
                downloadAFileUseCase(context)(item, context).collect { itr ->
                    binding.progressBar.progress = itr

                    endTime = System.currentTimeMillis()
                    endBytes = item.bytesCopied

                    val seconds = ((endTime.toDouble() - startTime.toDouble()) / 1000.0)
                    if(seconds > 1){
                        val result = LogicUtil.calculateDownloadSpeed(seconds, startBytes, endBytes)

                        binding.textView.text = result + " - "+ item.convertToSizeUnit() + " - " + item.downloadState.toString()
                        withContext(Dispatchers.IO){
                            notification.builder.setContentText(result + " - "+ item.convertToSizeUnit() + " - " + item.downloadState.toString())
                            notification.showProgress(context, item)
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
                            notification.builder.setContentText(binding.textView.text)
                            notification.showProgress(context, item)
                            notification.showNotification(context, item)
                            ExternalUse.insertToListUseCase(context)(item)
                        }

                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return DownloadItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context)
    }
}
class DownloadListDiffCallback: DiffUtil.ItemCallback<StrucDownFile>() {
    override fun areItemsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem.downloadLink == newItem.downloadLink
    }

    override fun areContentsTheSame(oldItem: StrucDownFile, newItem: StrucDownFile): Boolean {
        return oldItem == newItem
    }
}