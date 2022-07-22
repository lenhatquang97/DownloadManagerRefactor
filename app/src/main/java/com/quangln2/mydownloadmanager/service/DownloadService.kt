package com.quangln2.mydownloadmanager.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.quangln2.mydownloadmanager.DownloadManagerApplication
import com.quangln2.mydownloadmanager.MainActivity
import com.quangln2.mydownloadmanager.R
import com.quangln2.mydownloadmanager.controller.DownloadManagerController
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.addToDownloadList
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.addToQueueList
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.createFileAgain
import com.quangln2.mydownloadmanager.controller.DownloadManagerController.findNextQueueDownloadFile
import com.quangln2.mydownloadmanager.data.constants.ConstantClass
import com.quangln2.mydownloadmanager.data.model.StructureDownFile
import com.quangln2.mydownloadmanager.data.model.downloadstatus.DownloadStatusState
import com.quangln2.mydownloadmanager.data.model.settings.GlobalSettings
import com.quangln2.mydownloadmanager.domain.DownloadAFileUseCase
import com.quangln2.mydownloadmanager.domain.UpdateToListUseCase
import com.quangln2.mydownloadmanager.util.DownloadUtil
import com.quangln2.mydownloadmanager.util.LogicUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DownloadService : Service() {

    private val CHANNEL_ID = "download_notification"
    private val binder = MyLocalBinder()
    private var builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
        .setContentTitle("DownloadManager")
        .setContentText("Welcome to DownloadManager")
        .setGroup(CHANNEL_ID)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    private var job: Job? = null


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class MyLocalBinder : Binder() {
        fun getService(): DownloadService {
            return this@DownloadService
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, builder.build())
    }


    private fun onOpenNotification(item: StructureDownFile) {
        val resultIntent = Intent(this@DownloadService, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? =
            TaskStackBuilder.create(this@DownloadService).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        builder = NotificationCompat.Builder(this@DownloadService, ConstantClass.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
            .setContentTitle(LogicUtil.cutFileName(item.fileName))
            .setContentText(item.downloadState.toString() + " " + item.convertBytesCopiedToSizeUnit())
            .setGroup(ConstantClass.CHANNEL_ID)
            .setContentIntent(resultPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val progress = (item.bytesCopied.toFloat() / item.size.toFloat() * 100).toInt()
        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder.setProgress(100, progress, false)
        manager.notify(item.id.hashCode(), builder.build())
        if (progress == 100) {
            manager.cancel(item.id.hashCode())
            job?.cancel()
            stopSelf()
            findNextQueueDownloadFile(this@DownloadService)
            return
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val item = intent.getSerializableExtra("item") as StructureDownFile
            val command = intent.getStringExtra("command") ?: "nothing"
            downloadAFileWithCreating(item, this, command)
        }
        return START_NOT_STICKY
    }


    private fun downloadAFileWithCreating(
        file: StructureDownFile,
        context: Context,
        command: String
    ) {
        if (command == "WaitForDownload") {
            val currentList = DownloadManagerController.downloadList.value
            if (currentList != null) {
                val numsOfDownloading =
                    currentList.count { it.downloadState == DownloadStatusState.DOWNLOADING }
                if (numsOfDownloading >= GlobalSettings.numsOfMaxDownloadThreadExported) {
                    addToQueueList(file)
                    return
                }
            }

        } else if (command == "KillNotification") {
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(file.id.hashCode())
            stopSelf()
            return
        }
        if (file.downloadState == DownloadStatusState.FAILED) {
            createFileAgain(file, context)
            val currentList = DownloadManagerController.downloadList.value
            val index = currentList?.indexOfFirst { it.id == file.id }
            if (index != null && index != -1) {
                currentList[index] = file.copy(downloadState = DownloadStatusState.DOWNLOADING)
                DownloadManagerController._downloadList.postValue(currentList)
            }
            CoroutineScope(Dispatchers.IO).launch {
                UpdateToListUseCase(DownloadManagerApplication.downloadRepository)(
                    file.copy()
                )
            }
        } else if (!DownloadUtil.isFileExisting(file, context) && command == "dequeue") {
            createFileAgain(file, context)
        } else if (!DownloadUtil.isFileExisting(file, context)) {
            createFileAgain(file, context)
            addToDownloadList(file)
        }
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == file.id }
        if (command == "dequeue") {
            if (index != null) {
                currentList[index] = file.copy()
            }
        }
        if (index != null && index != -1) {
            job = CoroutineScope(Dispatchers.Main).launch {
                DownloadAFileUseCase(DownloadManagerApplication.downloadRepository)(
                    currentList[index],
                    context
                ).collect {
                    DownloadManagerController._progressFile.value = it
                    onOpenNotification(it)
                    if (it.downloadState == DownloadStatusState.FAILED || it.downloadState == DownloadStatusState.PAUSED) {
                        findNextQueueDownloadFile(this@DownloadService)
                    }
                }
            }
        }
    }

}
