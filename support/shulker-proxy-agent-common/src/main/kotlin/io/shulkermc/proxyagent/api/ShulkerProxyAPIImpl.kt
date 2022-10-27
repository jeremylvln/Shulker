package io.shulkermc.proxyagent.api

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.api.adapters.DirectoryAdapterImpl
import io.shulkermc.proxyapi.ShulkerProxyAPI
import io.shulkermc.proxyapi.adapters.DirectoryAdapter

class ShulkerProxyAPIImpl(agent: ShulkerProxyAgentCommon): ShulkerProxyAPI {
    val directoryAdapter = DirectoryAdapterImpl(agent)

    init {
        ShulkerProxyAPI.INSTANCE = this
    }

    override fun getDirectoryAdapter(): DirectoryAdapter {
        return this.directoryAdapter
    }
}
