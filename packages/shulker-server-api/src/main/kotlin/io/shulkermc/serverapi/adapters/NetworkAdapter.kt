package io.shulkermc.serverapi.adapters

import java.util.concurrent.CompletableFuture

interface NetworkAdapter {
    fun setAllocated(): CompletableFuture<Unit>
    fun setReserved(seconds: Long): CompletableFuture<Unit>

    fun askShutdown()
}
