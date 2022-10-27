package io.shulkermc.proxyagent

import io.shulkermc.proxyagent.domain.Player
import io.shulkermc.proxyagent.domain.PlayerPreLoginHook
import io.shulkermc.proxyagent.domain.ServerPreConnectHook
import io.shulkermc.proxyapi.adapters.ServerName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class ProxyInterfaceBungeeCord(
    private val plugin: Plugin,
    private val proxy: ProxyServer
): ProxyInterface {
    override fun shutdown() {
        this.proxy.stop()
    }

    override fun registerServer(name: ServerName, address: InetSocketAddress) {
        this.proxy.servers[name] = this.proxy.constructServerInfo(name, address, "", false)
    }

    override fun unregisterServer(name: String) {
        this.proxy.servers.remove(name)
    }

    override fun hasServer(name: String): Boolean {
        return this.proxy.servers.containsKey(name)
    }

    override fun addServerPreConnectHook(hook: ServerPreConnectHook) {
        this.proxy.pluginManager.registerListener(this.plugin, object : Listener {
            @EventHandler(priority = EventPriority.LOWEST)
            private fun onServerConnect(event: ServerConnectEvent) {
                val result = hook(wrapPlayer(event.player), event.target.name)

                if (result.newServerName.isPresent)
                    event.target = proxy.servers[result.newServerName.get()]!!
            }
        })
    }

    override fun addPlayerPreLoginHook(hook: PlayerPreLoginHook) {
        this.proxy.pluginManager.registerListener(this.plugin, object : Listener {
            @EventHandler(priority = EventPriority.HIGHEST)
            private fun onPreLogin(event: PreLoginEvent) {
                val result = hook()

                if (!result.allowed)
                    event.setCancelReason(*BungeeComponentSerializer.get().serialize(result.rejectComponent!!))
            }
        })
    }

    override fun getPlayerCount(): Int {
        return this.proxy.players.size
    }

    override fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable) {
        this.proxy.scheduler.schedule(this.plugin, runnable, delay, timeUnit)
    }

    override fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable) {
        this.proxy.scheduler.schedule(this.plugin, runnable, delay, interval, timeUnit)
    }

    private fun wrapPlayer(bungeePlayer: ProxiedPlayer): Player {
        return object : Player {
            override fun disconnect(component: Component) {
                bungeePlayer.disconnect(*BungeeComponentSerializer.get().serialize(component))
            }
        }
    }
}
