package io.shulkermc.proxyagent.adapters.agones

interface AgonesGatewayAdapter {
    fun destroy()

    fun emitProxyReady()
    fun emitProxyShutdown()
}
