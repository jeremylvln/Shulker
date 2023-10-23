package io.shulkermc.serveragent.api

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class ShulkerServerAPIImpl(private val agent: ShulkerServerAgentCommon) : ShulkerServerAPI() {
    init {
        INSTANCE = this
    }

    override fun askShutdown() {
        try {
            this.agent.agonesGateway.askShutdown()
        } catch (ex: Exception) {
            this.agent.logger.severe("Failed to ask Agones sidecar to shutdown properly, stopping process manually")
            exitProcess(0)
        }
    }

    override fun setAllocated(): CompletableFuture<Void> = this.agent.agonesGateway.setAllocated().thenAccept {}
    override fun setReserved(seconds: Long): CompletableFuture<Void> = this.agent.agonesGateway.setReserved(seconds).thenAccept {}
}
