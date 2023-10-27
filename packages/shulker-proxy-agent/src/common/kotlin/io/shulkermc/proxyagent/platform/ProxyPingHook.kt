package io.shulkermc.proxyagent.platform

data class ProxyPingHookResult(val playerCount: Int);

typealias ProxyPingHook = () -> ProxyPingHookResult
