package io.shulkermc.serveragent.adapters.agones

import agones.dev.sdk.AgonesSDK

class AgonesGatewayAdapterImpl : AgonesGatewayAdapter {
    private val sdk = AgonesSDK.createFromEnvironment()

    override fun destroy() {
        this.sdk.destroy()
    }

    override fun emitServerReady() {
        this.sdk.emitReady()
    }

    override fun emitServerAllocated() {
        this.sdk.emitAllocated()
    }

    override fun emitServerShutdown() {
        this.sdk.emitShutdown()
    }
}
