package com.quangln2.mydownloadmanager

import android.content.Context
import androidx.fragment.app.Fragment

fun Fragment.getViewModelFactory(context: Context): ViewModelFactory {
    val repository = (requireContext().applicationContext as DownloadManagerApplication).downloadRepository
    return ViewModelFactory(repository, context)
}