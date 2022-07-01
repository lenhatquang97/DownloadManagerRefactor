package com.quangln2.mydownloadmanager.ui.home

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.*
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding
import com.quangln2.mydownloadmanager.service.DownloadService
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.downloadAFileUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.pauseDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.resumeDownloadUseCase
import com.quangln2.mydownloadmanager.ui.externaluse.ExternalUse.Companion.retryDownloadUseCase
import com.quangln2.mydownloadmanager.util.LogicUtil
import com.quangln2.mydownloadmanager.util.LogicUtil.Companion.cutFileName
import com.quangln2.mydownloadmanager.util.UIComponentUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.util.concurrent.Executors

class DownloadListAdapter(private var context: Context): ListAdapter<StrucDownFile, DownloadListAdapter.DownloadItemViewHolder>(
    AsyncDifferConfig.Builder(DownloadListDiffCallback()).setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {
    class DownloadItemViewHolder private constructor(private val binding: DownloadItemBinding): RecyclerView.ViewHolder(binding.root){
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

            item.bytesCopied = ExternalUse.getBytesFromExistingFileUseCase(context)(item,context)

            val intent = Intent(context, DownloadService::class.java)
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
                            val intent = Intent(Intent.ACTION_VIEW)
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                intent.setDataAndType(item.uri, item.mimeType)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            } else {
                                val file = File(item.downloadTo)
                                if(file.exists()){
                                    val uri = Uri.fromFile(file)
                                    intent.setDataAndType(uri, item.mimeType)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                            if (intent.resolveActivity(context.packageManager) == null) {
                                Toast.makeText(context, "There is no application to open this file", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                context.startActivity(intent)
                            }
                    }
                    DownloadStatusState.FAILED -> {
                        item.downloadState = DownloadStatusState.DOWNLOADING
                        binding.progressBar.progress = 0
                        binding.progressBar.visibility = View.VISIBLE
                        retryDownloadUseCase(context)(item, context)
                        downloadAFileWithProgressBar(binding, item, context)
                        binding.downloadStateButton.setImageResource(R.drawable.ic_pause)
                    }
                    else -> {

                    }
                }
                binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    intent.putExtra("fileName", cutFileName(item.fileName))
                    intent.putExtra("content", binding.textView.text)
                    intent.putExtra("id", item.id.hashCode())
                    context.startForegroundService(intent)
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
                        intent.putExtra("fileName", cutFileName(item.fileName))
                        intent.putExtra("content", item.convertToSizeUnit() + " - " + item.downloadState.toString())
                        intent.putExtra("id", item.id.hashCode())
                        context.startForegroundService(intent)
                        ExternalUse.updateToListUseCase(context)(item)
                    }


                }
            }
            if(item.downloadState == DownloadStatusState.DOWNLOADING){
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

            val intent = Intent(context, DownloadService::class.java)

            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO){
                    intent.putExtra("fileName", cutFileName(item.fileName))
                    intent.putExtra("content", item.convertToSizeUnit() + " - " + item.downloadState.toString())
                    intent.putExtra("id", item.id.hashCode())
                    context.startForegroundService(intent)
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
                            intent.putExtra("fileName", cutFileName(item.fileName))
                            intent.putExtra("content", result + " - "+ item.convertToSizeUnit() + " - " + item.downloadState.toString())
                            intent.putExtra("progress", itr)
                            intent.putExtra("id", item.id.hashCode())
                            context.startForegroundService(intent)
                        }

                        startBytes = endBytes
                        startTime = endTime
                    }

                    if(itr == 100){

                        //Show on UI
                        item.downloadState = DownloadStatusState.COMPLETED
                        binding.stopButton.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
                        binding.downloadStateButton.setImageResource(R.drawable.ic_open)

                        withContext(Dispatchers.IO){
                            //Show notification
                            intent.putExtra("fileName", cutFileName(item.fileName))
                            intent.putExtra("content", binding.textView.text)
                            intent.putExtra("progress", 100)
                            intent.putExtra("id", item.id.hashCode())
                            context.startForegroundService(intent)

                            //Update to list
                            ExternalUse.updateToListUseCase(context)(item)
                            ExternalUse.howManyFileDownloading -= 1
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