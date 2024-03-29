package io.shulkermc.proxyagent.platform

import java.util.Optional

data class ServerPreConnectHookResult(val newServerName: Optional<String>)

typealias ServerPreConnectHook = (player: Player, originalServerName: String) -> ServerPreConnectHookResult
