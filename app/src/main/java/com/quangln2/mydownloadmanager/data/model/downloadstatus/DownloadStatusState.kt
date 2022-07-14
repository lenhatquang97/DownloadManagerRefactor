package com.quangln2.mydownloadmanager.data.model.downloadstatus

enum class DownloadStatusState {
    ALL {
        override fun toString(): String {
            return "All"
        }
    },
    DOWNLOADING {
        override fun toString(): String {
            return "Downloading"
        }
    },
    PAUSED {
        override fun toString(): String {
            return "Paused"
        }
    },
    COMPLETED {
        override fun toString(): String {
            return "Completed"
        }
    },
    FAILED {
        override fun toString(): String {
            return "Failed"
        }
    },
    QUEUED {
        override fun toString(): String {
            return "Queued"
        }
    },
    MIDDLE {
        override fun toString(): String {
            return "Middle"
        }
    }
}