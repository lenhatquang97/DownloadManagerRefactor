package com.quangln2.downloadmanagerrefactor.data.source.local

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.core.content.FileProvider
import com.quangln2.downloadmanagerrefactor.BuildConfig
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.constants.ConstantClass
import com.quangln2.downloadmanagerrefactor.data.database.DownloadDao
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.source.protocol.HttpProtocol
import com.quangln2.downloadmanagerrefactor.service.DownloadService
import com.quangln2.downloadmanagerrefactor.util.DownloadUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class LocalDataSourceImpl(
    private val downloadDao: DownloadDao
) : LocalDataSource {
    override suspend fun insert(file: StructureDownFile, context: Context) = downloadDao.insert(file, context)
    override suspend fun update(file: StructureDownFile, context: Context) = downloadDao.update(file, context)
    override suspend fun deleteFromDatabase(StructureDownFile: StructureDownFile, context: Context) =
        downloadDao.delete(StructureDownFile, context)

    override fun doesDownloadLinkExist(file: StructureDownFile, context: Context): Flow<Boolean> =
        downloadDao.doesDownloadLinkExist(file.downloadLink, context)

    override suspend fun deletePermanently(file: StructureDownFile, context: Context) {
        val filePath = File(file.downloadTo + '/' + file.fileName)
        if (filePath.exists()) {
            filePath.delete()
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ConstantClass.FILE_NOT_FOUND_DELETE_FROM_LIST, Toast.LENGTH_SHORT).show()
            }
        }

        val numberOfChunks = if (file.protocol == "Socket") DownloadManagerController.numberOfSocketChunks
        else HttpProtocol.numberOfHTTPChunks

        (0 until numberOfChunks).forEach {
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.chunkNames[it])
            if (appSpecificExternalDir.exists()) {
                appSpecificExternalDir.delete()
            }
        }


        val intent = Intent(context, DownloadService::class.java)
        DownloadManagerController.newItem.postValue(file)
        intent.putExtra("command", "KillNotification")
        context.startService(intent)
    }


    override fun writeToFile(file: StructureDownFile) {
        if (file.fileName.contains(".")) {
            val extension = file.fileName.substring(file.fileName.lastIndexOf("."))
            val name = file.fileName.substring(0, file.fileName.lastIndexOf("."))
            file.fileName = name + "_" + UUID.randomUUID().toString().substring(0, 4) + extension
        } else {
            file.fileName = file.fileName + "_" + UUID.randomUUID().toString().substring(0, 4)
        }
        file.downloadTo = (file.downloadTo.ifEmpty {
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).absolutePath
        })
    }

    override fun openDownloadFile(item: StructureDownFile, context: Context) {
        val doesFileExist = DownloadUtil.isFileExisting(item, context)
        if (!doesFileExist) return
        val intent = Intent(Intent.ACTION_VIEW)
        val file = File(item.downloadTo + '/' + item.fileName)
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            intent.setDataAndType(uri, item.mimeType)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            Toast.makeText(context, ConstantClass.NO_APPLICATION_TO_OPEN, Toast.LENGTH_SHORT)
                .show()
            val extension = item.fileName.substring(item.fileName.lastIndexOf("."))
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://search?q=${extension}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/search?q=${extension}")
                    )
                )
            }
        } else {
            context.startActivity(intent)
        }
    }

    override fun vibratePhone(context: Context) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vib.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }
    }

}