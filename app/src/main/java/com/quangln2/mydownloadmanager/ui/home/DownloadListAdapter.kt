package com.quangln2.mydownloadmanager.ui.home

import android.view.LayoutInflater
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
            binding.textView.text = item.convertToSizeUnit()
            binding.progressBar.progress = 50
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