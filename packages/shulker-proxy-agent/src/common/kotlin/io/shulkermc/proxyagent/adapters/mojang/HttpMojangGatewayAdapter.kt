package io.shulkermc.proxyagent.adapters.mojang

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.HttpURLConnection
import java.net.URI
import java.util.Optional
import java.util.UUID

class HttpMojangGatewayAdapter : MojangGatewayAdapter {
    override fun getProfileFromName(playerName: String): Optional<MojangGatewayAdapter.MojangProfile> {
        val url = URI("https://api.mojang.com/users/profiles/minecraft/$playerName").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val status = connection.responseCode
        if (status != 200) return Optional.empty()

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val responseJson = JsonParser.parseString(response).asJsonObject
        return Optional.of(this.getProfileFromJson(responseJson))
    }

    override fun getProfileFromId(playerId: UUID): Optional<MojangGatewayAdapter.MojangProfile> {
        val undashedPlayerId = playerId.toString().replace("-", "")
        val url = URI("https://sessionserver.mojang.com/session/minecraft/profile/$undashedPlayerId").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val status = connection.responseCode
        if (status != 200) return Optional.empty()

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val responseJson = JsonParser.parseString(response).asJsonObject
        return Optional.of(this.getProfileFromJson(responseJson))
    }

    private fun getProfileFromJson(json: JsonObject): MojangGatewayAdapter.MojangProfile {
        val uuid = UUID.fromString(
            json.get("id").asString.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
            )
        )
        val name = json.get("name").asString

        return MojangGatewayAdapter.MojangProfile(uuid, name)
    }
}
