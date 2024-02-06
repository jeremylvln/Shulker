package io.shulkermc.proxyagent.adapters.filesystem

import java.nio.file.Files
import java.nio.file.Path

private val DRAIN_LOCK_PATH = Path.of("/tmp/drain-lock")
private val READINESS_LOCK_PATH = Path.of("/tmp/readiness-lock")

class LocalFileSystemAdapter : FileSystemAdapter {
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
}
