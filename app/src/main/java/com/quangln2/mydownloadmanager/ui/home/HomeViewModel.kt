package com.quangln2.mydownloadmanager.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    private val _items = MutableLiveData<List<Int>>().apply { value = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }
    val items: MutableLiveData<List<Int>> = _items
}