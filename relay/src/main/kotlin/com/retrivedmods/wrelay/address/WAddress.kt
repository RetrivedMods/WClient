package com.retrivedmods.wrelay.address

import java.net.InetAddress
import java.net.InetSocketAddress

data class WAddress(val hostName: String, val port: Int)

inline val WAddress.inetSocketAddress: InetSocketAddress
    get() {
        return try {
            val addresses = InetAddress.getAllByName(hostName)
            if (addresses.isNotEmpty()) {
                InetSocketAddress(addresses[0], port)
            } else {
                InetSocketAddress(hostName, port)
            }
        } catch (e: Exception) {
            println("DNS resolution failed for $hostName, using unresolved address: ${e.message}")
            InetSocketAddress.createUnresolved(hostName, port)
        }
    }

