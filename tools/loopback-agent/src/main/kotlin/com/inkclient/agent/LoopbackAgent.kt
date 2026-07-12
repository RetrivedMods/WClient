package com.inkclient.agent

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

/**
 * Simple loopback agent stub for local testing of GlobalResourcePackChanger.
 * Listens on 127.0.0.1:19133 and replies with a simple ACK for each message.
 * Run this on your development machine (not on Android device) with a JVM.
 */
fun main() {
    val port = 19133
    val server = ServerSocket(port)
    println("InkClient Loopback Agent listening on 127.0.0.1:$port")

    while (true) {
        val socket = server.accept()
        thread(start = true) {
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
                val writer = PrintWriter(s.getOutputStream(), true, Charsets.UTF_8)
                val line = reader.readLine()
                println("[InkAgent] Received: $line")
                // Here you would implement actual resource pack application logic.
                writer.println("ACK")
            }
        }
    }
}
