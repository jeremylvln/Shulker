package io.shulkermc.proxyapi

import io.shulkermc.proxyapi.adapters.DirectoryAdapter

interface ShulkerProxyAPI {
    fun getDirectoryAdapter(): DirectoryAdapter
}
