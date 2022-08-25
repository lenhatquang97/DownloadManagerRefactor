package com.quangln2.downloadmanagerrefactor.data.source.protocol

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.quangln2.downloadmanagerrefactor.ServiceLocator
import com.quangln2.downloadmanagerrefactor.controller.DownloadManagerController
import com.quangln2.downloadmanagerrefactor.data.model.FromTo
import com.quangln2.downloadmanagerrefactor.data.model.StructureDownFile
import com.quangln2.downloadmanagerrefactor.data.model.downloadstatus.DownloadStatusState
import com.quangln2.downloadmanagerrefactor.util.UIComponentUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.net.Socket
import java.util.*

class SocketProtocol : ProtocolInterface, Serializable {
    var ip = ""
    var port = 0
    var socket: Socket? = null
    var inp: DataInputStream? = null
    var out: DataOutputStream? = null
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    constructor(ip: String, port: Int) {
        this.ip = ip
        this.port = port
        scope.launch { connectToServer(ip, port) }
    }

    companion object {
        fun createJSONRule(command: String, content: String): String {
            val obj = JSONObject()
            obj.put("command", command)
            obj.put("content", content)
            return obj.toString()
        }
    }

    private fun sendMessageWithSyntaxCommandContent(mess: String) {
        val command = mess.split("|")[0]
        val content = mess.split("|")[1]
        scope.launch {
            out?.write("${createJSONRule(command, content)}\n".toByteArray())
            out?.flush()
        }

    }

    private suspend fun connectToServer(ip: String, port: Int) {
        try {
            socket = Socket(ip, port)
            if (socket != null) {
                inp = DataInputStream(socket!!.getInputStream())
                out = DataOutputStream(socket!!.getOutputStream())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun closeConnection() {
        try{
            scope.launch {
                out?.write("${createJSONRule("exit", "bye")}\n".toByteArray())
                out?.flush()
                socket?.close()
                inp?.close()
                out?.close()
            }
        } catch (e: Exception) {
            Log.d("SocketProtocol", "closeConnection: " + e.message)
        }

    }

    override fun addNewDownloadInfo(url: String, downloadTo: String, file: StructureDownFile) {
        file.id = UUID.randomUUID().toString()
        file.downloadLink = url
        file.downloadTo = downloadTo
        file.bytesCopied = 0
        file.fileName = url.substring(url.lastIndexOf("/") + 1)
        file.mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            file.fileName.substring(file.fileName.lastIndexOf(".") + 1)
        )
            .toString()
        file.chunkNames =
            (0 until 1).map { UUID.randomUUID().toString() }.toMutableList()
        file.kindOf = UIComponentUtil.defineTypeOfCategoriesBasedOnFileName(file.mimeType)
    }

    override fun fetchDownloadInfo(file: StructureDownFile): StructureDownFile {
        try {
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                sendMessageWithSyntaxCommandContent("getFileInfo|${file.fileName}")
                var result = inp?.readLine()
                while(result == null){
                    result = inp?.readLine()
                    //Create timer for timeout
                }
                val jsonObj = JSONObject(result)
                file.size = jsonObj.getLong("fileSize")
                file.listChunks = (0 until 1).map {
                    val tmp = file.size / 1
                    val endVal =
                        if (it == 0) file.size else tmp * (it + 1) - 1
                    FromTo(tmp * it, endVal, tmp * it)
                }.toMutableList()
            }
            return file
        } catch (e: Exception) {
            //Exception
            Log.d("FileError", e.toString())
            val initFile = ServiceLocator.initializeStructureDownFile()
            file.fileName = initFile.fileName
            file.size = initFile.size
            return initFile
        }
    }

    override fun downloadAFile(file: StructureDownFile, context: Context): Flow<StructureDownFile> = flow {
        val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.chunkNames[0])
        val fos = FileOutputStream(appSpecificExternalDir.absolutePath, true)

        val fileNameSegmented = file.downloadLink.split("/")[1]
        out?.write("${createJSONRule("sendFile", fileNameSegmented)}\n".toByteArray())
        out?.flush()

        var bytesRead = inp?.readBytes()

        while (bytesRead != null && bytesRead.isNotEmpty()) {
            file.bytesCopied += bytesRead.size
            fos.write(bytesRead)
            emit(file)
            bytesRead = inp?.readBytes()
        }
    }

    override fun resumeDownload(file: StructureDownFile, context: Context) {
        scope.launch {
            connectToServer(ip, port)
            val appSpecificExternalDir = File(context.getExternalFilesDir(null), file.fileName)
            sendMessageWithSyntaxCommandContent("resume|${file.fileName}?${appSpecificExternalDir.length()}")
            file.downloadState = DownloadStatusState.RESUMED
            downloadAFile(file, context)
        }
    }

    override fun pauseDownload(file: StructureDownFile) {
        scope.launch {
            socket?.close()
            sendMessageWithSyntaxCommandContent("pause|${file.fileName}")
        }
    }

    override fun stopDownload(item: StructureDownFile, context: Context) {
        scope.launch {
            socket?.close()
            sendMessageWithSyntaxCommandContent("stop|${item.fileName}")
            val file = File(context.getExternalFilesDir(null), item.fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    override fun retryDownload(file: StructureDownFile, context: Context) {
        scope.launch {
            connectToServer(ip, port)
            sendMessageWithSyntaxCommandContent("retry|${file.fileName}")
            downloadAFile(file, context)
        }
    }

}