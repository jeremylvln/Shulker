package io.shulkermc.proxyagent.adapters.filesystem

import java.nio.file.Files
import java.nio.file.Path

private val DRAIN_LOCK_PATH = Path.of("/tmp/drain-lock")

class LocalFileSystemAdapter : FileSystemAdapter {
    override fun createDrainFile() {
        if (!Files.exists(DRAIN_LOCK_PATH)) {
            Files.createFile(DRAIN_LOCK_PATH)
        }
    }
}
