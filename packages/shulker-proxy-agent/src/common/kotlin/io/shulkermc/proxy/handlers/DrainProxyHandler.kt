package io.shulkermc.proxy.handlers

import io.shulkermc.proxy.Configuration
import io.shulkermc.proxy.ShulkerProxyAgentCommon

class DrainProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(proxyName: String) {
        if (Configuration.PROXY_NAME != proxyName) {
            return
        }

        this.agent.proxyLifecycleService.drain()
    }
}
