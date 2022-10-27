package io.shulkermc.proxyapi

import io.shulkermc.proxyapi.adapters.DirectoryAdapter

interface ShulkerProxyAPI {
    companion object {
        lateinit var INSTANCE: ShulkerProxyAPI
    }

    fun getDirectoryAdapter(): DirectoryAdapter
}
