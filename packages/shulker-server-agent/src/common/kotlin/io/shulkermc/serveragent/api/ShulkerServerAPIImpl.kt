package io.shulkermc.serveragent.api

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import java.util.concurrent.CompletableFuture

class ShulkerServerAPIImpl(private val agent: ShulkerServerAgentCommon) : ShulkerServerAPI() {
    override fun askShutdown() = this.agent.shutdown()

    override fun setAllocated(): CompletableFuture<Void> = this.agent.agonesGateway.setAllocated().thenAccept {}
    override fun setReserved(seconds: Long): CompletableFuture<Void> = this.agent.agonesGateway.setReserved(seconds).thenAccept {}
}
