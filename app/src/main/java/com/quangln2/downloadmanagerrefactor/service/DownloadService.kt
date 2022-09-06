package com.quangln2.downloadmanagerrefactor.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.quangln2.downloadmanagerrefactor.DownloadManagerApplication
import com.quangln2.downloadmanagerrefactor.MainActivity
import com.quangln2.downloadmanagerrefactor.R
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.addToDownloadList
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.addToQueueList
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.createFileAgain
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.findNextQueueDownloadFile
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController.speedController
import com.quangln2.downloadmanagerrefactor.controller.DownloadSpeedController
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass.Companion.CHANNEL_ID
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.data.model.settings.GlobalSettings
import com.quangln2.downloadmanagerrefactor.data.source.protocol.HttpProtocol
import com.quangln2.downloadmanagerrefactor.domain.local.UpdateToListUseCase
import com.quangln2.downloadmanagerrefactor.domain.remote.DownloadAFileUseCase
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil.Companion.combineFile
import com.quangln2.downloadmanagerrefactor.util.LogicUtil
import com.quangln2.downloadmanagerrefactor.util.LogicUtil.Companion.roundSize
import kotlinx.coroutines.*


class DownloadService : Service() {
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val binder = MyLocalBinder()
    private var builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
        .setContentTitle(ConstantClass.WELCOME_TITLE)
        .setContentText(ConstantClass.WELCOME_CONTENT)
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

        val progress = (item.bytesCopied.toFloat() / item.size.toFloat() * 100).toInt()
        val contentView = RemoteViews(packageName, R.layout.custom_notification)
        contentView.apply {
            setTextViewText(R.id.contentTitle, item.fileName)
            setTextViewText(R.id.contentText, item.textProgressFormat)
            setProgressBar(R.id.progressBar, 100, progress, false)
        }

        builder = NotificationCompat.Builder(this@DownloadService, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
            .setContent(contentView)
            .setOngoing(true)
        //            .setContentIntent(resultPendingIntent)
        val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(item.id.hashCode(), builder.build())

        if (item.bytesCopied >= item.size) {
            manager.cancel(item.id.hashCode())
            scope.launch {
                combineFile(item, this@DownloadService, if (item.protocol == "Socket") 1 else HttpProtocol.numberOfHTTPChunks)
            }
            job?.cancelChildren()
            stopSelf()
            findNextQueueDownloadFile(this@DownloadService)
            return
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val item = DownloadManagerController.newItem.value!!
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
        val speed = DownloadSpeedController()
        speedController.value?.put(file.id, speed.copy(startTimes = System.currentTimeMillis()))
        if (command == "WaitForDownload") {
            val currentList = DownloadManagerController.downloadList.value
            if (currentList != null) {
                val numsOfDownloading =
                    currentList.count { it.downloadState == DownloadStatusState.DOWNLOADING }
                if (numsOfDownloading >= GlobalSettings.numsOfMaxDownloadThreadExported) {
                    addToQueueList(file, context)
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
            createFileAgain(file)
            val currentList = DownloadManagerController.downloadList.value
            val index = currentList?.indexOfFirst { it.id == file.id }
            if (index != null && index != -1) {
                currentList[index] = file.copy(downloadState = DownloadStatusState.DOWNLOADING)
                DownloadManagerController._downloadList.postValue(currentList)
            }
            scope.launch {
                UpdateToListUseCase(DownloadManagerApplication.downloadRepository)(
                    file.copy(),
                    context
                )
            }
        } else if (!DownloadUtil.isFileExisting(file, context) && command == "dequeue") {
            createFileAgain(file)
        } else if (!DownloadUtil.isFileExisting(file, context)) {
            createFileAgain(file)
            addToDownloadList(file.copy(), context)
        }
        val currentList = DownloadManagerController.downloadList.value
        val index = currentList?.indexOfFirst { it.id == file.id }
        if (command == "dequeue") {
            if (index != null) {
                currentList[index] = file.copy()
            }
        }
        if (index != null && index != -1) {
            job = scope.launch {
                DownloadAFileUseCase(DownloadManagerApplication.downloadRepository)(
                    currentList[index],
                    context
                ).collect { it ->
                    withContext(Dispatchers.IO) {
                        DownloadManagerController._progressFile.postValue(it)
                    }
                    withContext(Dispatchers.Main) {
                        onOpenNotification(it)
                        onCalculateDownloadProgress(it)
                    }
                    if (!DownloadUtil.isNetworkAvailable(context)) {
                        DownloadManagerController._downloadList.value?.forEach { it ->
                            if (it.downloadState == DownloadStatusState.FAILED || it.downloadState == DownloadStatusState.DOWNLOADING) {
                                it.downloadState = DownloadStatusState.FAILED
                                DownloadManagerController._progressFile.postValue(it)
                                onOpenNotification(it)
                            }
                        }
                    }
                    if (it.downloadState == DownloadStatusState.FAILED || it.downloadState == DownloadStatusState.PAUSED) {
                        findNextQueueDownloadFile(this@DownloadService)
                    }
                }
            }
        }
    }

    private fun onCalculateDownloadProgress(item: StructureDownFile) {
        if (item.downloadState == DownloadStatusState.PAUSED || item.downloadState == DownloadStatusState.FAILED) {
            item.textProgressFormat =
                "${item.convertBytesCopiedToSizeUnit()} of ${item.convertToSizeUnit()}"
            return
        }
        if (speedController.value != null && speedController.value?.containsKey(item.id)!!) {
            speedController.value!![item.id]?.endTimes = System.currentTimeMillis()
            speedController.value!![item.id]?.endBytes = item.bytesCopied
            val seconds =
                ((speedController.value!![item.id]?.endTimes?.toDouble()!! - (speedController.value!![item.id]?.startTimes?.toDouble()!!)) / 1000.0)
            val result = LogicUtil.calculateDownloadSpeed(
                seconds,
                speedController.value!![item.id]?.startBytes!!,
                speedController.value!![item.id]?.endBytes!!
            )
            if (seconds > 0.8 && result > 0 && item.downloadState == DownloadStatusState.DOWNLOADING) {
                item.textProgressFormat =
                    "${roundSize(result)} - ${item.convertBytesCopiedToSizeUnit()} of ${item.convertToSizeUnit()}"
                speedController.value!![item.id]?.startBytes = speedController.value!![item.id]?.endBytes!!
                speedController.value!![item.id]?.startTimes = speedController.value!![item.id]?.endTimes!!
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
