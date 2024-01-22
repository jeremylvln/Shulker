package io.shulkermc.serveragent.paper

import io.shulkermc.serveragent.ServerInterface
import io.shulkermc.serveragent.paper.scheduler.ServerScheduler
import io.shulkermc.serveragent.paper.scheduler.ServerSchedulerFolia
import io.shulkermc.serveragent.paper.scheduler.ServerSchedulerPaper
import io.shulkermc.serveragent.platform.HookPostOrder
import io.shulkermc.serveragent.platform.PlayerDisconnectHook
import io.shulkermc.serveragent.platform.PlayerLoginHook
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.EventExecutor
import java.util.UUID
import java.util.concurrent.TimeUnit

class ServerInterfacePaper(private val plugin: ShulkerServerAgentPaper) : ServerInterface {
    companion object {
        private fun isFoliaContext(): Boolean {
            return try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
                true
            } catch (ignored: ClassNotFoundException) {
                false
            }
        }
    }

    private val eventListener = object : Listener {}

    private val scheduler: ServerScheduler = if (isFoliaContext()) {
        ServerSchedulerFolia(this.plugin)
    } else {
        ServerSchedulerPaper(this.plugin)
    }

    override fun prepareNetworkAdminsPermissions(playerIds: List<UUID>) {
        this.registerEventWithPriority(PlayerLoginEvent::class.java, HookPostOrder.FIRST) { event ->
            if (event.player.uniqueId in playerIds) {
                event.player.isOp = true
                // TODO: This may not be sufficient to give them all permissions
                //       but there is solution to grant all of them without knowing them
            }
        }
    }

    override fun addPlayerLoginHook(hook: PlayerLoginHook, postOrder: HookPostOrder) {
        this.registerEventWithPriority(PlayerLoginEvent::class.java, postOrder) { event ->
            hook(wrapPlayer(event.player))
        }
    }

    override fun addPlayerDisconnectHook(hook: PlayerDisconnectHook, postOrder: HookPostOrder) {
        this.registerEventWithPriority(PlayerQuitEvent::class.java, postOrder) { event ->
            hook(wrapPlayer(event.player))
        }
    }

    override fun getPlayerCount(): Int {
        return this.plugin.server.onlinePlayers.size
    }

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask = this.scheduler.scheduleDelayedTask(delay, timeUnit, runnable)

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask = this.scheduler.scheduleRepeatingTask(delay, interval, timeUnit, runnable)

    private fun <T : Event> registerEventWithPriority(
        clazz: Class<T>,
        postOrder: HookPostOrder,
        callback: (event: T) -> Unit
    ) {
        val executor = EventExecutor { _, event ->
            @Suppress("UNCHECKED_CAST")
            callback(event as T)
        }

        this.plugin.server.pluginManager.registerEvent(
            clazz,
            this.eventListener,
            this.mapPostOrder(postOrder),
            executor,
            this.plugin
        )
    }

    private fun wrapPlayer(paperPlayer: Player): io.shulkermc.serveragent.platform.Player {
        return object : io.shulkermc.serveragent.platform.Player {
            override val uniqueId: UUID
                get() = paperPlayer.uniqueId

            override val name: String
                get() = paperPlayer.name
        }
    }

    private fun mapPostOrder(postOrder: HookPostOrder): EventPriority {
        return when (postOrder) {
            HookPostOrder.FIRST -> EventPriority.LOWEST
            HookPostOrder.EARLY -> EventPriority.LOW
            HookPostOrder.NORMAL -> EventPriority.NORMAL
            HookPostOrder.LATE -> EventPriority.HIGH
            HookPostOrder.LAST -> EventPriority.HIGHEST
            HookPostOrder.MONITOR -> EventPriority.MONITOR
        }
    }
}
