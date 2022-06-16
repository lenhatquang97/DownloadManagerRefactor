package com.quangln2.mydownloadmanager.ui.state.downloadstatus

import java.util.*

class CompletedUIState: DownloadStatusUiState{
    private var completedDownDate: Date
    private var timeDone: Int
    constructor(timeDone: Int){
        this.timeDone = timeDone
        completedDownDate = Date()
    }

}