package io.shulkermc.serveragent.api.adapters

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serverapi.adapters.NetworkAdapter

class NetworkAdapterImpl(
    private val agent: ShulkerServerAgentCommon
) : NetworkAdapter {
    override fun markAllocated() {
        this.agent.agonesGateway!!.emitServerAllocated()
    }
}
