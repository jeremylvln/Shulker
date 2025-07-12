package io.shulkermc.clusterapi.impl.adapters.mojang

import io.shulkermc.cluster.data.MojangProfile
import java.util.Optional
import java.util.UUID

interface MojangGatewayAdapter {
    fun getProfileFromName(playerName: String): Optional<MojangProfile>

    fun getProfileFromId(playerId: UUID): Optional<MojangProfile>
}
