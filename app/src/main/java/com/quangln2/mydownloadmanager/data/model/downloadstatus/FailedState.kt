package com.quangln2.mydownloadmanager.data.model.downloadstatus

class FailedState: DownloadStatusState() {
    override fun toString(): String {
        return "Failed"
    }
}