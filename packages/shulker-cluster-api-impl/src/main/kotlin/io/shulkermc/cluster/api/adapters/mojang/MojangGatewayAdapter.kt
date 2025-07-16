package io.shulkermc.cluster.api.adapters.mojang

import io.shulkermc.cluster.api.data.MojangProfile
import java.util.Optional
import java.util.UUID

interface MojangGatewayAdapter {
    fun getProfileFromName(playerName: String): Optional<MojangProfile>

    fun getProfileFromId(playerId: UUID): Optional<MojangProfile>
}
