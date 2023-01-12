package io.shulkermc.proxyagent

import io.shulkermc.proxyagent.domain.PlayerPreLoginHook
import io.shulkermc.proxyagent.domain.ServerPreConnectHook
import io.shulkermc.proxyapi.adapters.ServerName
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

interface ProxyInterface {
    fun shutdown()

    fun registerServer(name: ServerName, address: InetSocketAddress)
    fun unregisterServer(name: String)
    fun hasServer(name: String): Boolean

    fun addServerPreConnectHook(hook: ServerPreConnectHook)
    fun addPlayerPreLoginHook(hook: PlayerPreLoginHook)

    fun getPlayerCount(): Int

    fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable)
    fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable)
}
