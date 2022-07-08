package com.quangln2.mydownloadmanager.data.model.settings

import android.os.Build

object GlobalSettings {
    var isVibrated = false
    var showOnLockScreen = false
    var showPopUpMessage = false
    var folderPath = "/storage/emulated/0/Download"

    fun changeFolderPath(path: String){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            folderPath = path
        }
    }
    fun openCloseLockScreen(){
        showOnLockScreen = !showOnLockScreen
    }
    fun openCloseVibrated(){
        isVibrated = !isVibrated
    }
    fun openClosePopUpMessage(){
        showPopUpMessage = !showPopUpMessage
    }
}