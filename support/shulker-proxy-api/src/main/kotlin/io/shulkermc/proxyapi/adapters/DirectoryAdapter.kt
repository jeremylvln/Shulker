package io.shulkermc.proxyapi.adapters

typealias ServerName = String
typealias ServerTag = String

interface DirectoryAdapter {
    fun getServersByTag(tag: ServerTag): Set<ServerName>
}
