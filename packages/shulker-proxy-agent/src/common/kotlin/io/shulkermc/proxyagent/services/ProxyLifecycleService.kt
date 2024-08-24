package io.shulkermc.proxyagent.services

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class ProxyLifecycleService(private val agent: ShulkerProxyAgentCommon) {
    companion object {
        private const val PROXY_DRAIN_ANNOTATION = "proxy.shulkermc.io/drain"
        private const val PROXY_DRAIN_CHECK_DELAY_SECONDS = 30L
    }

    private val ttlTask: ProxyInterface.ScheduledTask
    private var drainingFuture: CompletableFuture<Unit>? = null

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

        this.ttlTask =
            this.agent.proxyInterface.scheduleDelayedTask(
                Configuration.PROXY_TTL_SECONDS,
                TimeUnit.SECONDS,
            ) { this.agent.shutdown() }
    }

    fun destroy() {
        this.ttlTask.cancel()
    }

    fun drain(): CompletableFuture<Unit> {
        if (this.drainingFuture != null) {
            return this.drainingFuture!!
        }

        this.drainingFuture = CompletableFuture<Unit>()
        this.agent.fileSystem.createDrainLock()

        // TODO: Rather than hardcoding a task, wait for Kubernetes to
        // exclude the proxy from the Service.
        this.agent.proxyInterface.scheduleDelayedTask(
            @Suppress("MagicNumber") 30L,
            TimeUnit.SECONDS,
        ) {
            this.drainingFuture?.complete(null)
        }

        this.agent.logger.info("Proxy is now draining")

        return this.drainingFuture!!.thenApply {
            this.onExcludedFromKubernetes()
        }
    }

    fun isDraining() = this.drainingFuture != null

    private fun onExcludedFromKubernetes() {
        this.agent.logger.info("Proxy was excluded from Kubernetes Service")
        this.agent.playerMovementService.setAcceptingPlayers(false)

        this.agent.proxyInterface.scheduleRepeatingTask(
            PROXY_DRAIN_CHECK_DELAY_SECONDS,
            PROXY_DRAIN_CHECK_DELAY_SECONDS,
            TimeUnit.SECONDS,
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
