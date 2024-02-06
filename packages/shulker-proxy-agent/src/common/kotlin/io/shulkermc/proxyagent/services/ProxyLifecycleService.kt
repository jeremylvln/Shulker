package io.shulkermc.proxyagent.services

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import java.util.concurrent.TimeUnit

class ProxyLifecycleService(private val agent: ShulkerProxyAgentCommon) {
    companion object {
        private const val PROXY_DRAIN_ANNOTATION = "proxy.shulkermc.io/drain"
        private const val PROXY_DRAIN_CHECK_DELAY_SECONDS = 30L
    }

    private val ttlTask: ProxyInterface.ScheduledTask
    private var drained = false

    init {
        this.agent.kubernetesGateway.watchProxyEvents { action, proxy ->
            this.agent.logger.fine("Detected modification on Proxy '${proxy.metadata.name}'")
            if (action == WatchAction.MODIFIED) {
                val annotations = proxy.metadata.annotations ?: return@watchProxyEvents
                if (annotations.getOrDefault(PROXY_DRAIN_ANNOTATION, "false") == "true") {
                    this.drain()
                }
            }
        }

        this.agent.logger.info("Proxy will be force stopped in ${Configuration.PROXY_TTL_SECONDS} seconds")
        this.ttlTask = this.agent.proxyInterface.scheduleDelayedTask(
            Configuration.PROXY_TTL_SECONDS,
            TimeUnit.SECONDS
        ) { this.agent.shutdown() }
    }

    fun destroy() {
        this.ttlTask.cancel()
    }

    private fun drain() {
        if (this.drained) {
            return
        }
        this.drained = true

        this.agent.fileSystem.createDrainLock()
        this.agent.playerMovementService.setAcceptingPlayers(false)

        this.agent.proxyInterface.scheduleRepeatingTask(
            PROXY_DRAIN_CHECK_DELAY_SECONDS,
            PROXY_DRAIN_CHECK_DELAY_SECONDS,
            TimeUnit.SECONDS
        ) {
            val playerCount = this.agent.proxyInterface.getPlayerCount()

            if (playerCount == 0) {
                this.agent.logger.info("Proxy is empty, stopping")
                this.agent.shutdown()
            } else {
                this.agent.logger.info("There are still $playerCount players connected, waiting")
            }
        }
    }
}
