package com.quangln2.mydownloadmanager.ui.home

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.data.model.StrucDownFile
import com.quangln2.mydownloadmanager.databinding.DownloadItemBinding

class DownloadListAdapter: ListAdapter<StrucDownFile, DownloadListAdapter.DownloadItemViewHolder>(DownloadListDiffCallback()) {

    class DownloadItemViewHolder private constructor(private val binding: DownloadItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: StrucDownFile){

            binding.heading.text = item.fileName
            binding.textView.text = item.convertToSizeUnit() + " - " + item.downloadState.toString()
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.VISIBLE

            var i = binding.progressBar.progress

            Thread {
                // this loop will run until the value of i becomes 99
                //item.bytesCopied / item.size * 100
                while (i < 100) {
                    i = ((item.bytesCopied / item.size) * 100).toInt()
                    // Update the progress bar and display the current value
                    Handler(Looper.getMainLooper()).post {
                        binding.progressBar.progress = i
                    }
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

            }.start()
        }
        companion object{
            fun from(parent: ViewGroup): DownloadItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DownloadItemBinding.inflate(layoutInflater, parent, false)
                return DownloadItemViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        return DownloadItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
        val item = getItem(position)
        println("Bind viewholder " + item.fileName)
        holder.bind(item)
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