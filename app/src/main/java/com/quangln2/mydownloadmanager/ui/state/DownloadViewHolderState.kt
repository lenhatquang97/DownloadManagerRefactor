package com.quangln2.mydownloadmanager.ui.state

import com.quangln2.mydownloadmanager.ui.state.downloadstatus.DownloadStatusUiState
import java.math.BigInteger

data class DownloadViewHolderState(
    var fileType: String = "",
    var fileName: String = "",
    var downloadSpeed: BigInteger,
    var totalSize: BigInteger,
    var downloadUIState: DownloadStatusUiState,
)