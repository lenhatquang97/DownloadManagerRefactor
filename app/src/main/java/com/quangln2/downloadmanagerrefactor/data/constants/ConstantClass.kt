package com.quangln2.downloadmanagerrefactor.data.constants

class ConstantClass {
    companion object {
        const val KB = 1024L
        const val MB = 1048576L
        const val GB = 1073741824L

        const val CHANNEL_ID = "download_notification"
        const val CHANNEL_NAME = "Download Notification"
        const val CHANNEL_DESCRIPTION = "This is a notification channel for downloading"

        const val DOWNLOAD_MESSAGE = "Do you want to download this file? This will cost "
        const val DOWNLOAD_AGAIN_MESSAGE =
            "You have downloaded this file. Do you want to download it again?"

        const val FILE_NAME_DEFAULT = "test"

        const val POSITIVE_BUTTON = "OK"
        const val NEGATIVE_BUTTON = "CANCEL"

        const val INVALID_URL = "Invalid URL"
        const val INVALID_LINK = "This link cannot be downloaded"

        const val CHECK_IP_PORT_PATH = """([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})\:[0-9]{1,5}(\/)(.*)"""
    }
}