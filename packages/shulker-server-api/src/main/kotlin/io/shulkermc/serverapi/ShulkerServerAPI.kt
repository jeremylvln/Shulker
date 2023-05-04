package io.shulkermc.serverapi

import io.shulkermc.serverapi.adapters.NetworkAdapter

abstract class ShulkerServerAPI {
    companion object {
        lateinit var INSTANCE: ShulkerServerAPI
    }

    abstract fun getNetworkAdapter(): NetworkAdapter
}
