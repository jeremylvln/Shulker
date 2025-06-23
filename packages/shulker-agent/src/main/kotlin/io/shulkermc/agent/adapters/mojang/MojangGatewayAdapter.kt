package io.shulkermc.agent.adapters.mojang

import java.util.Optional
import java.util.UUID

interface MojangGatewayAdapter {
    fun getProfileFromName(playerName: String): Optional<MojangProfile>

    fun getProfileFromId(playerId: UUID): Optional<MojangProfile>

    data class MojangProfile(val playerId: UUID, val playerName: String)
}
