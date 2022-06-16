package com.quangln2.mydownloadmanager.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quangln2.mydownloadmanager.R

class DownloadListAdapter(private val data: MutableList<Int>): RecyclerView.Adapter<DownloadListAdapter.DownloadItemViewHolder>() {
    override fun getItemCount(): Int{
        return data.size
    }
    class DownloadItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.download_item, parent, false)
        return DownloadItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DownloadItemViewHolder, position: Int) {
    }

}