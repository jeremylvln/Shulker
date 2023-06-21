package io.shulkermc.api

import io.shulkermc.api.domain.GameServer
import io.shulkermc.api.domain.NamespaceKey
import java.util.concurrent.CompletableFuture

interface ShulkerAPI {
    fun summonGameServer(fleetNamespaceKey: NamespaceKey): CompletableFuture<GameServer>
}
