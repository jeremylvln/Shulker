package io.shulkermc.serveragent.api

import com.agones.dev.sdk.GameServer
import io.shulkermc.serveragent.ShulkerServerAgentCommon
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class ShulkerServerAPIImpl(private val agent: ShulkerServerAgentCommon) : ShulkerServerAPI() {
    override fun askShutdown() = this.agent.shutdown()

    override fun setReady(): CompletableFuture<Void> = this.agent.agonesGateway.setReady().thenAccept {}
    override fun watchGameServer(consumer: Consumer<GameServer>) {
        return this.agent.agonesGateway.watchGameServer(consumer)
    }

    override fun setAllocated(): CompletableFuture<Void> = this.agent.agonesGateway.setAllocated().thenAccept {}

    override fun setReserved(seconds: Long): CompletableFuture<Void> =
        this.agent.agonesGateway.setReserved(seconds).thenAccept {}
}
