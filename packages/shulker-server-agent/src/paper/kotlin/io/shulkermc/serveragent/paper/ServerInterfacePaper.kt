package io.shulkermc.serveragent.paper

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import io.shulkermc.serveragent.ServerInterface
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.util.UUID
import java.util.concurrent.TimeUnit

class ServerInterfacePaper(private val plugin: ShulkerServerAgentPaper) : ServerInterface {
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
    ): ServerInterface.ScheduledTask {
        return PaperScheduledTask(
            this.plugin.server.asyncScheduler.runDelayed(this.plugin, { runnable.run() }, delay, timeUnit)
        )
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask {
        return PaperScheduledTask(
            this.plugin.server.asyncScheduler.runAtFixedRate(this.plugin, { runnable.run() }, delay, interval, timeUnit)
        )
    }

    private class PaperScheduledTask(private val scheduledTask: ScheduledTask) : ServerInterface.ScheduledTask {
        override fun cancel() {
            if (!this.scheduledTask.isCancelled) this.scheduledTask.cancel()
        }
    }
}
