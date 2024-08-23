package io.shulkermc.proxyagent.handlers

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon

class DrainProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(proxyName: String) {
        if (Configuration.PROXY_NAME != proxyName) {
            return
        }

        this.agent.proxyLifecycleService.drain()
    }
}
