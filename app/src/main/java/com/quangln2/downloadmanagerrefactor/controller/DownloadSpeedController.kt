package com.quangln2.downloadmanagerrefactor.controller

data class DownloadSpeedController(
    var startTimes: Long = -1,
    var endTimes: Long = -1,
    var startBytes: Long = 0,
    var endBytes: Long = 0
)