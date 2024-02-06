package io.shulkermc.proxyagent.adapters.filesystem

interface FileSystemAdapter {
    fun createDrainLock()

    fun createReadinessLock()
    fun deleteReadinessLock()
}
