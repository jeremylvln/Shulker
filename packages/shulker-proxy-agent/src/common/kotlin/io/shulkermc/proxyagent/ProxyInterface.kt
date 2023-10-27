package io.shulkermc.proxyagent

import io.shulkermc.proxyagent.platform.PlayerDisconnectHook
import io.shulkermc.proxyagent.platform.PlayerLoginHook
import io.shulkermc.proxyagent.platform.PlayerPreLoginHook
import io.shulkermc.proxyagent.platform.ProxyPingHook
import io.shulkermc.proxyagent.platform.ServerPostConnectHook
import io.shulkermc.proxyagent.platform.ServerPreConnectHook
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

interface ProxyInterface {
    fun registerServer(name: String, address: InetSocketAddress)
    fun unregisterServer(name: String): Boolean
    fun hasServer(name: String): Boolean

    fun addProxyPingHook(hook: ProxyPingHook)
    fun addPlayerPreLoginHook(hook: PlayerPreLoginHook)
    fun addPlayerLoginHook(hook: PlayerLoginHook)
    fun addPlayerDisconnectHook(hook: PlayerDisconnectHook)
    fun addServerPreConnectHook(hook: ServerPreConnectHook)
    fun addServerPostConnectHook(hook: ServerPostConnectHook)

    fun teleportPlayerOnServer(playerName: String, serverName: String)
    fun getPlayerCount(): Int

    fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable): ScheduledTask
    fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable): ScheduledTask

    interface ScheduledTask {
        fun cancel()
    }
}
