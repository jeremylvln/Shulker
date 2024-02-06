@file:Suppress("detekt:SpreadOperator")

package io.shulkermc.proxyagent.bungeecord

import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.platform.HookPostOrder
import io.shulkermc.proxyagent.platform.Player
import io.shulkermc.proxyagent.platform.PlayerDisconnectHook
import io.shulkermc.proxyagent.platform.PlayerLoginHook
import io.shulkermc.proxyagent.platform.PlayerPreLoginHook
import io.shulkermc.proxyagent.platform.ProxyPingHook
import io.shulkermc.proxyagent.platform.ServerPostConnectHook
import io.shulkermc.proxyagent.platform.ServerPreConnectHook
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PermissionCheckEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
class ProxyInterfaceBungeeCord(
    private val plugin: Plugin,
    private val proxy: ProxyServer
) : ProxyInterface {
    override fun registerServer(name: String, address: InetSocketAddress) {
        this.proxy.servers[name] = this.proxy.constructServerInfo(name, address, "", false)
    }

    override fun unregisterServer(name: String): Boolean {
        return this.proxy.servers.remove(name) != null
    }

    override fun hasServer(name: String): Boolean {
        return this.proxy.servers.containsKey(name)
    }

    override fun addProxyPingHook(hook: ProxyPingHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.LOWEST)
                fun onPreLogin(event: ProxyPingEvent) {
                    val result = hook()
                    event.response.players.online = result.onlinePlayerCount
                    event.response.players.max = result.maxPlayerCount
                }
            }
        )
    }

    override fun addPlayerPreLoginHook(hook: PlayerPreLoginHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.LOWEST)
                fun onPreLogin(event: PreLoginEvent) {
                    if (event.isCancelled) return
                    val result = hook()

                    if (!result.allowed) {
                        event.reason = TextComponent.fromArray(
                            *BungeeComponentSerializer.get().serialize(result.rejectComponent!!)
                        )
                    }
                }
            }
        )
    }

    override fun addPlayerLoginHook(hook: PlayerLoginHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.LOW)
                fun onLogin(event: PostLoginEvent) {
                    hook(wrapPlayer(event.player))
                }
            }
        )
    }

    override fun addPlayerDisconnectHook(hook: PlayerDisconnectHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.HIGH)
                fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
                    hook(wrapPlayer(event.player))
                }
            }
        )
    }

    override fun addServerPreConnectHook(hook: ServerPreConnectHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.LOWEST)
                fun onServerConnect(event: ServerConnectEvent) {
                    if (event.isCancelled) return
                    val result = hook(wrapPlayer(event.player), event.target.name)

                    if (result.newServerName.isPresent) {
                        @Suppress("UnsafeCallOnNullableType")
                        event.target = proxy.servers[result.newServerName.get()]!!
                    }
                }
            }
        )
    }

    override fun addServerPostConnectHook(hook: ServerPostConnectHook, postOrder: HookPostOrder) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                // NOTE: BungeeCord does not support runtime event priority
                @EventHandler(priority = EventPriority.HIGH)
                fun onServerConnected(event: ServerConnectedEvent) {
                    hook(wrapPlayer(event.player), event.server.info.name)
                }
            }
        )
    }

    override fun prepareNetworkAdminsPermissions(playerIds: List<UUID>) {
        this.proxy.pluginManager.registerListener(
            this.plugin,
            object : Listener {
                @EventHandler(priority = EventPriority.HIGHEST)
                fun onPermissionCheck(event: PermissionCheckEvent) {
                    val player = event.sender as? ProxiedPlayer ?: return
                    if (playerIds.contains(player.uniqueId)) {
                        event.setHasPermission(true)
                    }
                }
            }
        )
    }

    override fun teleportPlayerOnServer(playerName: String, serverName: String) {
        val server = this.proxy.getServerInfo(serverName)

        if (server != null) {
            this.proxy.getPlayer(playerName)?.connect(server)
        }
    }

    override fun getPlayerCount(): Int {
        return this.proxy.players.size
    }

    override fun getPlayerCapacity(): Int {
        return this.proxy.config.playerLimit
    }

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ProxyInterface.ScheduledTask {
        return BungeeCordScheduledTask(this.proxy.scheduler.schedule(this.plugin, runnable, delay, timeUnit))
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ProxyInterface.ScheduledTask {
        return BungeeCordScheduledTask(this.proxy.scheduler.schedule(this.plugin, runnable, delay, interval, timeUnit))
    }

    private fun wrapPlayer(bungeePlayer: ProxiedPlayer): Player {
        return object : Player {
            override val uniqueId: UUID
                get() = bungeePlayer.uniqueId

            override val name: String
                get() = bungeePlayer.name

            override fun disconnect(component: Component) {
                bungeePlayer.disconnect(*BungeeComponentSerializer.get().serialize(component))
            }
        }
    }

    private class BungeeCordScheduledTask(private val bungeeCordTask: ScheduledTask) : ProxyInterface.ScheduledTask {
        override fun cancel() {
            this.bungeeCordTask.cancel()
        }
    }
}
