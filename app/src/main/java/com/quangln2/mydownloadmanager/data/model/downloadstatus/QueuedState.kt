package com.quangln2.mydownloadmanager.data.model.downloadstatus

class QueuedState: DownloadStatusState {
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