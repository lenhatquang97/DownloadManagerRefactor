package com.quangln2.mydownloadmanager.ui.state.downloadstatus

import java.math.BigInteger

class DownloadingUiState : DownloadStatusUiState{
    private var timeRemaining: Int
    private var downloadSpeed: BigInteger
    constructor(timeRemaining: Int, downloadSpeed: BigInteger) {
        this.timeRemaining = timeRemaining
        this.downloadSpeed = downloadSpeed
    }

}
