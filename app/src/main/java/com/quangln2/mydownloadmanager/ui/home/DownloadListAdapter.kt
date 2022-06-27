package com.quangln2.mydownloadmanager.ui.home

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.notification.DownloadNotification
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
                DownloadManagerApplication.writeToFileAPI29AboveUseCase(item, context)
            } else {
                DownloadManagerApplication.writeToFileAPI29BelowUseCase(item)
            }
            notification = DownloadNotification(context, item)

            binding.downloadStateButton.setOnClickListener {
                when(item.downloadState){
                    is DownloadingState -> {
                        DownloadManagerApplication.pauseDownloadUseCase(item)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_start)
                    }
                    is PausedState -> {
                        DownloadManagerApplication.resumeDownloadUseCase(item)
                        downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    is CompletedState -> {
                        item.downloadState = CompletedState(0)
                        binding.stopButton.visibility = View.GONE
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                    }
                    is FailedState -> {
                        item.downloadState = DownloadingState(0,0)
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.VISIBLE
                        DownloadManagerApplication.retryDownloadUseCase(item, context)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            DownloadManagerApplication.writeToFileAPI29AboveUseCase(item, context)
                        } else {
                            DownloadManagerApplication.writeToFileAPI29BelowUseCase(item)
                        }
                        downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                }
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                //notification.builder.setContentText(binding.textView.text)
                //notification.showNotification(context, item)
            }
            binding.stopButton.setOnClickListener {
                if(item.downloadState is DownloadingState || item.downloadState is PausedState){
                    item.downloadState = FailedState()
                    binding.progressBar.visibility = View.GONE
                    binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                    binding.downloadStateButton.setImageResource(R.drawable.ic_retry)
                    //notification.builder.setContentText(item.convertToSizeUnit() + " - " + item.downloadState.toString())
                    //notification.showNotification(context, item)
                }
            }
            downloadAFileWithProgressBar(binding, item, context)


        }
        companion object{
            fun from(parent: ViewGroup): DownloadItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DownloadItemBinding.inflate(layoutInflater, parent, false)
                return DownloadItemViewHolder(binding)
            }
        }

        private fun downloadAFileWithProgressBar(binding: DownloadItemBinding, item: StrucDownFile, context: Context){
            CoroutineScope(Dispatchers.Main).launch {
                //notification.builder.setContentTitle(cutFileName(item.fileName))
                //notification.showNotification(context, item)
                DownloadManagerApplication.downloadAFileUseCase(item, context).collect { itr ->
                    binding.heading.text = cutFileName(item.fileName)
                    binding.progressBar.progress = itr
                    //notification.showProgress(context, item)
                    if(itr == 100){
                        item.downloadState = CompletedState(0)
                        binding.stopButton.visibility = View.GONE
                        binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)
                    }
                }
            }
        }
        private fun cutFileName(fileName: String): String{
            return if(fileName.length < 25) fileName else fileName.substring(0,10) + "..." + fileName.substring(fileName.length - 10, fileName.length)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return DownloadItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val item = getItem(position)
        println("Bind view holder " + item.fileName)
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