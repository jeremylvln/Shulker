package io.shulkermc.proxyapi

import io.shulkermc.proxyapi.adapters.DirectoryAdapter

abstract class ShulkerProxyAPI {
    companion object {
        lateinit var INSTANCE: ShulkerProxyAPI
    }

    abstract fun getDirectoryAdapter(): DirectoryAdapter
}
