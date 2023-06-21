package io.shulkermc.serveragent.api.adapters

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serverapi.adapters.NetworkAdapter
import kotlin.system.exitProcess

class NetworkAdapterImpl(
    private val agent: ShulkerServerAgentCommon
) : NetworkAdapter {
    override fun setAllocated() = this.agent.agonesGateway.setAllocated()
    override fun setReserved(seconds: Long) = this.agent.agonesGateway.setReserved(seconds)

    override fun askShutdown() {
        try {
            this.agent.agonesGateway.askShutdown()
        } catch (ex: Exception) {
            this.agent.logger.severe("Failed to ask Agones sidecar to shutdown properly, stopping process manually")
            exitProcess(0)
        }
    }
}
