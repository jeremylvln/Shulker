package io.shulkermc.proxy.tasks

import io.shulkermc.proxy.ProxyInterface
import io.shulkermc.proxy.ShulkerProxyAgentCommon
import java.util.concurrent.TimeUnit

class HealthcheckTask(private val agent: ShulkerProxyAgentCommon) : Runnable {
    companion object {
        private const val HEALTHCHECK_INTERVAL_SECONDS = 5L
    }

    fun schedule(): ProxyInterface.ScheduledTask {
        return this.agent.proxyInterface.scheduleRepeatingTask(
            0L,
            HEALTHCHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS,
            this,
        )
    }

    override fun run() {
        this.agent.cluster.agonesGateway.sendHealthcheck()
        this.agent.cluster.cache.updateProxyLastSeen(this.agent.cluster.selfReference.name)
    }
}
