package com.quangln2.mydownloadmanager.ui.state.downloadstatus

import java.math.BigInteger

class PausedUiState: DownloadStatusUiState{
    private var currentSize: BigInteger = BigInteger.ZERO
    constructor(currentSize: BigInteger) : super() {
        this.currentSize = currentSize
    }
}