package io.shulkermc.proxyagent

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import io.shulkermc.proxyagent.domain.PlayerPreLoginHook
import io.shulkermc.proxyagent.domain.ServerPreConnectHook
import io.shulkermc.proxyapi.adapters.ServerName
import net.kyori.adventure.text.Component
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class ProxyInterfaceVelocity(
    private val plugin: BootstrapPlugin,
    private val proxy: ProxyServer
): ProxyInterface {
    override fun shutdown() {
        this.proxy.shutdown()
    }

    override fun registerServer(name: ServerName, address: InetSocketAddress) {
        this.proxy.registerServer(ServerInfo(name, address))
    }

    override fun unregisterServer(name: String) {
        this.proxy.getServer(name).ifPresent { registeredServer ->
            this.proxy.unregisterServer(registeredServer.serverInfo)
        }
    }

    override fun hasServer(name: String): Boolean {
        return this.proxy.getServer(name).isPresent
    }

    override fun addServerPreConnectHook(hook: ServerPreConnectHook) {
        this.proxy.eventManager.register(plugin, ServerPreConnectEvent::class.java, PostOrder.LAST) { event ->
            val result = hook(this.wrapPlayer(event.player), event.originalServer.serverInfo.name)

            if (result.newServerName.isPresent)
                event.result = ServerPreConnectEvent.ServerResult.allowed(this.proxy.getServer(result.newServerName.get()).get())
        }
    }

    override fun addPlayerPreLoginHook(hook: PlayerPreLoginHook) {
        this.proxy.eventManager.register(plugin, PreLoginEvent::class.java, PostOrder.FIRST) { event ->
            val result = hook()

            if (!result.allowed)
                event.result = PreLoginEvent.PreLoginComponentResult.denied(result.rejectComponent)
        }
    }

    override fun getPlayerCount(): Int {
        return this.proxy.playerCount
    }

    override fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable) {
        this.proxy.scheduler
            .buildTask(this.plugin, runnable)
            .delay(delay, timeUnit)
            .schedule()
    }

    override fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable) {
        this.proxy.scheduler
            .buildTask(this.plugin, runnable)
            .delay(delay, timeUnit)
            .repeat(interval, timeUnit)
            .schedule()
    }

    private fun wrapPlayer(velocityPlayer: Player): io.shulkermc.proxyagent.domain.Player {
        return object : io.shulkermc.proxyagent.domain.Player {
            override fun disconnect(component: Component) {
                velocityPlayer.disconnect(component)
            }
        }
    }
}
