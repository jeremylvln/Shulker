package io.shulkermc.proxyagent.tasks

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.concurrent.TimeUnit

class LostProxyPurgeTask(private val agent: ShulkerProxyAgentCommon) : Runnable {
    companion object {
        private const val PROXY_LOST_MILLIS_THRESHOLD = 1000L * 60 * 5
    }

    fun schedule(): ProxyInterface.ScheduledTask {
        return this.agent.proxyInterface.scheduleRepeatingTask(1L, 1L, TimeUnit.MINUTES, this)
    }

    override fun run() {
        val maybeLock = this.agent.cache.tryLockLostProxiesPurgeTask(Configuration.PROXY_NAME, 15L)

        if (maybeLock.isPresent) {
            val lock = maybeLock.get()
            this.agent.logger.info("Purging lost proxies")

            this.agent.cache.listRegisteredProxies()
                .filter { System.currentTimeMillis() - it.lastSeenMillis > PROXY_LOST_MILLIS_THRESHOLD }
                .forEach {
                    this.agent.cache.unregisterProxy(it.proxyName)
                    this.agent.logger.info("Unregistered lost proxy ${it.proxyName}")
                }

            lock.release()
        }
    }
}
