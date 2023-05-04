package io.shulkermc.serveragent.adapters.agones

interface AgonesGatewayAdapter {
    fun destroy()

    fun emitServerReady()
    fun emitServerAllocated()
    fun emitServerShutdown()
}
