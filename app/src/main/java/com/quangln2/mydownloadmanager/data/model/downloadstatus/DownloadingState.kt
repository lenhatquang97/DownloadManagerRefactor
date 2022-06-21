package com.quangln2.mydownloadmanager.data.model.downloadstatus

import java.math.BigInteger

class DownloadingState : DownloadStatusState {
    private var timeRemaining: Int
    private var downloadSpeed: Long
    constructor(timeRemaining: Int, downloadSpeed: Long) {
        this.timeRemaining = timeRemaining
        this.downloadSpeed = downloadSpeed
    }

}
