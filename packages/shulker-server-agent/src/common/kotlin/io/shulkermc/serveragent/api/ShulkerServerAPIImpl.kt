package io.shulkermc.serveragent.api

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serveragent.api.adapters.NetworkAdapterImpl
import io.shulkermc.serverapi.ShulkerServerAPI
import io.shulkermc.serverapi.adapters.NetworkAdapter

class ShulkerServerAPIImpl(agent: ShulkerServerAgentCommon) : ShulkerServerAPI() {
    private val networkAdapter = NetworkAdapterImpl(agent)

    init {
        INSTANCE = this
    }

    override fun getNetworkAdapter(): NetworkAdapter {
        return this.networkAdapter
    }
}
