package io.shulkermc.proxyagent.utils

import java.net.InetSocketAddress

fun addressFromHostString(
    hostAndPort: String,
    defaultPort: Int = 25565,
): InetSocketAddress {
    val parts = hostAndPort.split(":")

    return if (parts.size == 2) {
        InetSocketAddress(parts[0], parts[1].toInt())
    } else {
        InetSocketAddress(parts[0], defaultPort)
    }
}
