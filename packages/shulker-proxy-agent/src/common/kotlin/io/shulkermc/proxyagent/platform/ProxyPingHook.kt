package io.shulkermc.proxyagent.platform

data class ProxyPingHookResult(val onlinePlayerCount: Int, val maxPlayerCount: Int)

typealias ProxyPingHook = () -> ProxyPingHookResult
