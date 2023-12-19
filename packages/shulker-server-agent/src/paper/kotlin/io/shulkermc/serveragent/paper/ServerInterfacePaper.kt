package io.shulkermc.serveragent.paper

import io.shulkermc.serveragent.ServerInterface
import io.shulkermc.serveragent.paper.scheduler.ServerScheduler
import io.shulkermc.serveragent.paper.scheduler.ServerSchedulerFolia
import io.shulkermc.serveragent.paper.scheduler.ServerSchedulerPaper
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
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

    private val scheduler: ServerScheduler = if (isFoliaContext()) {
        ServerSchedulerFolia(this.plugin)
    } else {
        ServerSchedulerPaper(this.plugin)
    }

    override fun prepareNetworkAdminsPermissions(playerIds: List<UUID>) {
        this.plugin.server.pluginManager.registerEvents(
            object : Listener {
                @EventHandler(priority = EventPriority.HIGHEST)
                fun onPlayerLogin(event: PlayerLoginEvent) {
                    if (event.player.uniqueId in playerIds) {
                        event.player.isOp = true
                        // TODO: This may not be sufficient to give them all permissions
                        //       but there is solution to grant all of them without knowing them
                    }
                }
            },
            this.plugin
        )
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
}
