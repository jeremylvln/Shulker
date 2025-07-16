package io.shulkermc.proxy.handlers

import io.shulkermc.proxy.ShulkerProxyAgentCommon

class DrainProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(proxyName: String) {
        if (this.agent.cluster.selfReference.name != proxyName) {
            return
        }

        this.agent.proxyLifecycleService.drain()
    }
}
