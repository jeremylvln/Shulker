package io.shulkermc.proxyagent.tasks

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.concurrent.TimeUnit

class LostProxyPurgeTask(private val agent: ShulkerProxyAgentCommon) : Runnable {
    companion object {
        private const val PROXY_LOST_PURGE_INTERVAL_MINUTES = 1L
        private const val PROXY_LOST_MILLIS_THRESHOLD = 1000L * 60 * 5
    }

    fun schedule(): ProxyInterface.ScheduledTask {
        this.agent.logger.info("Lost proxy will be purged every $PROXY_LOST_PURGE_INTERVAL_MINUTES minutes")
        return this.agent.proxyInterface.scheduleRepeatingTask(
            PROXY_LOST_PURGE_INTERVAL_MINUTES,
            PROXY_LOST_PURGE_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
            this,
        )
    }

    override fun run() {
        val maybeLock = this.agent.cache.tryLockLostProxiesPurgeTask(Configuration.PROXY_NAME)

        maybeLock.ifPresent { lock ->
            lock.use { _ ->
                this.agent.cache.listRegisteredProxies()
                    .filter { System.currentTimeMillis() - it.lastSeenMillis > PROXY_LOST_MILLIS_THRESHOLD }
                    .forEach { proxy ->
                        this.agent.cache.unregisterProxy(proxy.proxyName)
                        this.agent.logger.info("Unregistered lost proxy ${proxy.proxyName}")
                    }
            }
        }
    }
}
