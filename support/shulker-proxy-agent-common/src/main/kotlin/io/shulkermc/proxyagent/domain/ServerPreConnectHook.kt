package io.shulkermc.proxyagent.domain

import io.shulkermc.proxyapi.adapters.ServerName
import java.util.Optional

data class ServerPreConnectHookResult(val newServerName: Optional<ServerName>)

typealias ServerPreConnectHook = (player: Player, originalServerName: String) -> ServerPreConnectHookResult
