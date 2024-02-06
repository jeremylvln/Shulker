package io.shulkermc.proxyagent.velocity

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.permission.PermissionsSetupEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.platform.HookPostOrder
import io.shulkermc.proxyagent.platform.PlayerDisconnectHook
import io.shulkermc.proxyagent.platform.PlayerLoginHook
import io.shulkermc.proxyagent.platform.PlayerPreLoginHook
import io.shulkermc.proxyagent.platform.ProxyPingHook
import io.shulkermc.proxyagent.platform.ServerPostConnectHook
import io.shulkermc.proxyagent.platform.ServerPreConnectHook
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrElse

@Suppress("TooManyFunctions")
class ProxyInterfaceVelocity(
    private val plugin: ShulkerProxyAgentVelocity,
    private val proxy: ProxyServer
) : ProxyInterface {
    override fun registerServer(name: String, address: InetSocketAddress) {
        this.proxy.registerServer(ServerInfo(name, address))
    }

    override fun unregisterServer(name: String): Boolean {
        return this.proxy.getServer(name).map { registeredServer ->
            this.proxy.unregisterServer(registeredServer.serverInfo)
            true
        }.getOrElse { false }
    }

    override fun hasServer(name: String): Boolean {
        return this.proxy.getServer(name).isPresent
    }

    override fun addProxyPingHook(hook: ProxyPingHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(
            this.plugin,
            ProxyPingEvent::class.java,
            this.mapPostOrder(postOrder)
        ) { event ->
            val result = hook()
            event.ping = event.ping.asBuilder()
                .onlinePlayers(result.onlinePlayerCount)
                .maximumPlayers(result.maxPlayerCount)
                .build()
        }
    }

    override fun addPlayerPreLoginHook(hook: PlayerPreLoginHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(
            this.plugin,
            PreLoginEvent::class.java,
            this.mapPostOrder(postOrder)
        ) { event ->
            if (!event.result.isAllowed) return@register
            val result = hook()

            if (!result.allowed) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(result.rejectComponent)
            }
        }
    }

    override fun addPlayerLoginHook(hook: PlayerLoginHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(this.plugin, LoginEvent::class.java, this.mapPostOrder(postOrder)) { event ->
            hook(wrapPlayer(event.player))
        }
    }

    override fun addPlayerDisconnectHook(hook: PlayerDisconnectHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(
            this.plugin,
            DisconnectEvent::class.java,
            this.mapPostOrder(postOrder)
        ) { event -> hook(this.wrapPlayer(event.player)) }
    }

    override fun addServerPreConnectHook(hook: ServerPreConnectHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(
            this.plugin,
            ServerPreConnectEvent::class.java,
            this.mapPostOrder(postOrder)
        ) { event ->
            if (!event.result.isAllowed) return@register
            val result = hook(this.wrapPlayer(event.player), event.originalServer.serverInfo.name)

            if (result.newServerName.isPresent) {
                event.result = ServerPreConnectEvent.ServerResult.allowed(
                    this.proxy.getServer(result.newServerName.get()).get()
                )
            }
        }
    }

    @Suppress("UnstableApiUsage")
    override fun addServerPostConnectHook(hook: ServerPostConnectHook, postOrder: HookPostOrder) {
        this.proxy.eventManager.register(
            this.plugin,
            ServerPostConnectEvent::class.java,
            this.mapPostOrder(postOrder)
        ) { event -> hook(this.wrapPlayer(event.player), event.player.currentServer.get().serverInfo.name) }
    }

    override fun prepareNetworkAdminsPermissions(playerIds: List<UUID>) {
        this.proxy.eventManager.register(
            this.plugin,
            PermissionsSetupEvent::class.java,
            PostOrder.LAST
        ) { event ->
            val player = event.subject as? Player ?: return@register
            if (playerIds.contains(player.uniqueId)) {
                event.provider = AdminPermissionProvider.INSTANCE
            }
        }
    }

    override fun teleportPlayerOnServer(playerName: String, serverName: String) {
        this.proxy.getPlayer(playerName).ifPresent { player ->
            this.proxy.getServer(serverName).ifPresent { server ->
                player.createConnectionRequest(server).fireAndForget()
            }
        }
    }

    override fun getPlayerCount(): Int {
        return this.proxy.playerCount
    }

    override fun getPlayerCapacity(): Int {
        return this.proxy.configuration.showMaxPlayers
    }

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ProxyInterface.ScheduledTask {
        return VelocityScheduledTask(
            this.proxy.scheduler
                .buildTask(this.plugin, runnable)
                .delay(delay, timeUnit)
                .schedule()
        )
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ProxyInterface.ScheduledTask {
        return VelocityScheduledTask(
            this.proxy.scheduler
                .buildTask(this.plugin, runnable)
                .delay(delay, timeUnit)
                .repeat(interval, timeUnit)
                .schedule()
        )
    }

    private fun wrapPlayer(velocityPlayer: Player): io.shulkermc.proxyagent.platform.Player {
        return object : io.shulkermc.proxyagent.platform.Player {
            override val uniqueId: UUID
                get() = velocityPlayer.uniqueId

            override val name: String
                get() = velocityPlayer.username

            override fun disconnect(component: Component) {
                velocityPlayer.disconnect(component)
            }
        }
    }

    private fun mapPostOrder(postOrder: HookPostOrder): PostOrder {
        return when (postOrder) {
            HookPostOrder.FIRST -> PostOrder.FIRST
            HookPostOrder.EARLY -> PostOrder.EARLY
            HookPostOrder.NORMAL -> PostOrder.NORMAL
            HookPostOrder.LATE -> PostOrder.LATE
            HookPostOrder.LAST -> PostOrder.LAST
            HookPostOrder.MONITOR -> PostOrder.LAST
        }
    }

    private class VelocityScheduledTask(private val velocityTask: ScheduledTask) : ProxyInterface.ScheduledTask {
        override fun cancel() {
            this.velocityTask.cancel()
        }
    }
}
