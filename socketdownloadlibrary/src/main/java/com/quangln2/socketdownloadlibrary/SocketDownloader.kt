package com.quangln2.socketdownloadlibrary

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class SocketDownloader {
    lateinit var socket: Socket
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun connectToServer(ip: String, port: Int, context: Context){
        scope.launch {
            socket = Socket(ip, port)
            if(socket.isConnected){
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                socket.close()
                scope.cancel()
            }
        }



    }
}