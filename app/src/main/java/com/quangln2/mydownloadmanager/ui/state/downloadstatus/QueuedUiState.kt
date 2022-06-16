package com.quangln2.mydownloadmanager.ui.state.downloadstatus

class QueuedUiState: DownloadStatusUiState{
    private var priority: Int = 0
    constructor(priority: Int) {
        this.priority = priority
    }
    public fun increasePriority() {
        priority++
    }
    public fun decreasePriority(){
        priority--
    }
}