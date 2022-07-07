package com.quangln2.mydownloadmanager.ui.home

import androidx.lifecycle.*

class HomeViewModel : ViewModel() {
    var _isOpenDialog = MutableLiveData<Boolean>().apply { value = false }
}