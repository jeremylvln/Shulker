package io.shulkermc.proxyagent.tasks

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.concurrent.TimeUnit

class HealthcheckTask(private val agent: ShulkerProxyAgentCommon) : Runnable {
    fun schedule(): ProxyInterface.ScheduledTask {
        return this.agent.proxyInterface.scheduleRepeatingTask(0L, 5L, TimeUnit.SECONDS, this)
    }

    override fun run() {
        this.agent.agonesGateway.sendHealthcheck()
        this.agent.cache.updateProxyLastSeen(Configuration.PROXY_NAME)
    }
}
