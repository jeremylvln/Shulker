package io.shulkermc.proxyagent.adapters.agones

import agones.dev.sdk.AgonesSDK

class AgonesGatewayAdapterImpl : AgonesGatewayAdapter {
    private val sdk = AgonesSDK.createFromEnvironment()

    override fun destroy() {
        this.sdk.destroy()
    }

    override fun emitProxyReady() {
        this.sdk.emitReady()
    }

    override fun emitProxyShutdown() {
        this.sdk.emitShutdown()
    }
}
