package com.quangln2.mydownloadmanager.data.model.downloadstatus

import java.math.BigInteger

class PausedState: DownloadStatusState() {
    override fun toString(): String {
        return "Paused"
    }
}