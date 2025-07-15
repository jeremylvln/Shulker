package io.shulkermc.proxy.platform

import java.util.Optional

data class ServerPreConnectHookResult(val allowed: Boolean, val newServerName: Optional<String>)

typealias ServerPreConnectHook = (player: Player, originalServerName: String) -> ServerPreConnectHookResult
