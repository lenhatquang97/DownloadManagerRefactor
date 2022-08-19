package com.quangln2.downloadmanagerrefactor.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.*

class SocketDownloadUtils {
    companion object{
        const val PORT = 28138
        fun openServerSocket(){
            CoroutineScope(Dispatchers.IO).launch {
                val serverSocket = ServerSocket(PORT)
                val socket = serverSocket.accept()
                val inputStream = DataInputStream(BufferedInputStream(socket.getInputStream()))
                var line = ""
                while (line != "exit") {
                    try{
                        line = inputStream.readUTF()
                        println(line)
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
                socket.close()
            }
        }

        fun openClientSocket(ip: String, port: Int){
            CoroutineScope(Dispatchers.IO).launch {
                val socket = Socket(ip, port)
                val outputStream = DataOutputStream(socket.getOutputStream())
                var line = ""
                while (line != "exit") {
                    try{
                        outputStream.write("Hello World".toByteArray())
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }

        }

        fun getIpAddress(){
            CoroutineScope(Dispatchers.IO).launch {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
                    println(socket.localAddress.hostAddress)
                }
            }
        }

    }

}