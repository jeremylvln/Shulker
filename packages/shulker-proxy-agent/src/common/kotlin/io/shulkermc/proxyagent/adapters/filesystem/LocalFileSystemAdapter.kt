package io.shulkermc.proxyagent.adapters.filesystem

import io.shulkermc.proxyagent.utils.addressFromHostString
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor
import org.apache.commons.io.monitor.FileAlterationMonitor
import org.apache.commons.io.monitor.FileAlterationObserver
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class LocalFileSystemAdapter : FileSystemAdapter {
    companion object {
        private const val EXTERNAL_SERVERS_WATCH_INTERVAL_MS = 10_000L

        private val EXTERNAL_SERVERS_PATH = Path.of("/mnt/shulker/external-servers/external-servers.yaml")
        private val DRAIN_LOCK_PATH = Path.of("/tmp/drain-lock")
        private val READINESS_LOCK_PATH = Path.of("/tmp/readiness-lock")
    }

    override fun createDrainLock() {
        if (!Files.exists(DRAIN_LOCK_PATH)) {
            Files.createFile(DRAIN_LOCK_PATH)
        }
    }

    override fun createReadinessLock() {
        if (!Files.exists(READINESS_LOCK_PATH)) {
            Files.createFile(READINESS_LOCK_PATH)
        }
    }

    override fun deleteReadinessLock() {
        Files.deleteIfExists(READINESS_LOCK_PATH)
    }

    override fun watchExternalServersUpdates(
        callback: (servers: Map<String, FileSystemAdapter.ExternalServer>) -> Unit,
    ) {
        val observer = FileAlterationObserver(EXTERNAL_SERVERS_PATH.parent.toFile())
        observer.addListener(
            object : FileAlterationListenerAdaptor() {
                override fun onFileChange(file: File) {
                    callback(parseExternalServersFile(file))
                }

                override fun onFileDelete(file: File) {
                    callback(emptyMap())
                }
            },
        )

        val monitor = FileAlterationMonitor(EXTERNAL_SERVERS_WATCH_INTERVAL_MS, observer)
        monitor.start()

        if (EXTERNAL_SERVERS_PATH.exists()) {
            callback(this.parseExternalServersFile(EXTERNAL_SERVERS_PATH.toFile()))
        }
    }

    private fun parseExternalServersFile(file: File): Map<String, FileSystemAdapter.ExternalServer> {
        val yaml = Yaml()
        val list: List<Map<String, Any>> = yaml.load(file.inputStream())

        @Suppress("UNCHECKED_CAST")
        return list.associate { entry ->
            val name = entry["name"] as String
            val address = entry["address"] as String
            val tags = entry.getOrDefault("tags", emptyList<String>()) as List<String>

            name to
                FileSystemAdapter.ExternalServer(
                    name,
                    addressFromHostString(address),
                    tags.toSet(),
                )
        }
    }
}
