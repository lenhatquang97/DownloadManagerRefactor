package com.quangln2.mydownloadmanager.data.model.downloadstatus

import java.util.*

class CompletedState: DownloadStatusState {
    private var completedDownDate: Date
    private var timeDone: Int
    constructor(timeDone: Int){
        this.timeDone = timeDone
        completedDownDate = Date()
    }

    override fun toString(): String {
        return "Completed"
    }

}