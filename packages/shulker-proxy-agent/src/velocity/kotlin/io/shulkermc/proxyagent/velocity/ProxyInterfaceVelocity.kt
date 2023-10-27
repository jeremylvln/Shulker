package io.shulkermc.proxyagent.velocity

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import io.shulkermc.proxyagent.ProxyInterface
import io.shulkermc.proxyagent.platform.PlayerPreLoginHook
import io.shulkermc.proxyagent.platform.ServerPreConnectHook
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrElse

class ProxyInterfaceVelocity(private val plugin: ShulkerProxyAgentVelocity, private val proxy: ProxyServer) : ProxyInterface {
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

    override fun addServerPreConnectHook(hook: ServerPreConnectHook) {
        this.proxy.eventManager.register(this.plugin, ServerPreConnectEvent::class.java, PostOrder.LAST) { event ->
            if (!event.result.isAllowed) return@register
            val result = hook(this.wrapPlayer(event.player), event.originalServer.serverInfo.name)

            if (result.newServerName.isPresent)
                event.result = ServerPreConnectEvent.ServerResult.allowed(this.proxy.getServer(result.newServerName.get()).get())
        }
    }

    override fun addPlayerPreLoginHook(hook: PlayerPreLoginHook) {
        this.proxy.eventManager.register(this.plugin, PreLoginEvent::class.java, PostOrder.FIRST) { event ->
            if (!event.result.isAllowed) return@register
            val result = hook()

            if (!result.allowed)
                event.result = PreLoginEvent.PreLoginComponentResult.denied(result.rejectComponent)
        }
    }

    override fun getPlayerCount(): Int {
        return this.proxy.playerCount
    }

    override fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable): ProxyInterface.ScheduledTask {
        return VelocityScheduledTask(
            this.proxy.scheduler
                .buildTask(this.plugin, runnable)
                .delay(delay, timeUnit)
                .schedule()
        )
    }

    override fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable): ProxyInterface.ScheduledTask {
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
            override fun disconnect(component: Component) {
                velocityPlayer.disconnect(component)
            }
        }
    }

    private class VelocityScheduledTask(private val velocityTask: ScheduledTask) : ProxyInterface.ScheduledTask {
        override fun cancel() {
            this.velocityTask.cancel()
        }
    }
}
