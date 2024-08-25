package io.shulkermc.proxyagent.adapters.filesystem

import java.net.InetSocketAddress

interface FileSystemAdapter {
    fun createDrainLock()

    fun createReadinessLock()

    fun deleteReadinessLock()

    fun watchExternalServersUpdates(
        callback: (servers: Map<String, ExternalServer>) -> Unit,
    )

    data class ExternalServer(val name: String, val address: InetSocketAddress, val tags: Set<String>)
}
