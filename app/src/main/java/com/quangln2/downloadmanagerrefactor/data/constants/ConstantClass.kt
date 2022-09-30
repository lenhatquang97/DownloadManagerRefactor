package com.quangln2.downloadmanagerrefactor.data.constants

class ConstantClass {
    companion object {
        const val KB = 1024L
        const val MB = 1048576L
        const val GB = 1073741824L

        const val WELCOME_TITLE = "DownloadManager"
        const val WELCOME_CONTENT = "Welcome to Download Manager"

        const val FILE_NAME_DEFAULT = "test"

        const val CHANNEL_ID = "download_notification"
        const val CHANNEL_NAME = "Download Notification"
        const val CHANNEL_DESCRIPTION = "This is a notification channel for downloading"

        const val DOWNLOAD_MESSAGE = "Do you want to download this file? This will cost "
        const val DOWNLOAD_AGAIN_MESSAGE = "You have downloaded this file. Do you want to download it again?"
        const val NO_APPLICATION_TO_OPEN = "There is no application to open this file"
        const val FILE_NOT_FOUND_DELETE_FROM_LIST = "File not found so we'll delete from list"

        const val POSITIVE_BUTTON = "OK"
        const val NEGATIVE_BUTTON = "CANCEL"

        const val INVALID_URL = "Invalid URL"
        const val INVALID_LINK = "This link cannot be downloaded"
        const val NOT_ENOUGH_SPACE = "Not enough space to download"

        const val CHECK_IP_PORT_PATH = """([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})\:[0-9]{1,5}(\/)(.*)"""

        const val DOWNLOAD_FOLDER_INTERNAL_PATH = "/storage/emulated/0/Download"
        const val MEDIA_TREE_FORMAT_DOWNLOAD_PATH = "/tree/downloads/document/downloads"

        const val RANDOM_SALT = "vVG9k3LzBlfteUp+O3+7zA=="

        const val DOWNLOAD_LIST_SEPARATOR = "%${RANDOM_SALT}"
        const val DOWNLOAD_LIST_CHUNK_SEPARATOR = "@${RANDOM_SALT}"
        const val DOWNLOAD_CHUNK_NAMES_SEPARATOR = "?${RANDOM_SALT}"
    }
}