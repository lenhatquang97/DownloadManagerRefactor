package com.quangln2.mydownloadmanager.data.constants

class ConstantClass {
    companion object{
        const val KB = 1024L
        const val MB = 1048576L
        const val GB = 1073741824L

        const val CHANNEL_ID = "download_notification"
        const val CHANNEL_NAME = "Download Notification"
        const val CHANNEL_DESCRIPTION = "This is a notification channel for downloading"

        const val DOWNLOAD_MESSAGE = "Do you want to download this file? This will cost "

        const val FILE_NAME_DEFAULT = "test"

        const val POSITIVE_BUTTON = "OK"
        const val NEGATIVE_BUTTON = "CANCEL"

        const val INVALID_URL = "Invalid URL"
        const val INVALID_LINK = "This link cannot be downloaded"
    }
}