package io.shulkermc.serveragent.paper

import io.shulkermc.serveragent.ServerInterface
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

class ServerInterfacePaper(private val plugin: ShulkerServerAgentPaper) : ServerInterface {
    companion object {
        private const val TICKS_PER_SECONDS = 20L
    }

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask {
        val delayTicks = timeUnit.toSeconds(delay) * TICKS_PER_SECONDS
        return BukkitScheduledTask(
            this.plugin.server.scheduler.runTaskLaterAsynchronously(this.plugin, runnable, delayTicks)
        )
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask {
        val delayTicks = timeUnit.toSeconds(delay) * TICKS_PER_SECONDS
        val intervalTicks = timeUnit.toSeconds(interval) * TICKS_PER_SECONDS
        return BukkitScheduledTask(
            this.plugin.server.scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delayTicks, intervalTicks)
        )
    }

    private class BukkitScheduledTask(private val bukkitTask: BukkitTask) : ServerInterface.ScheduledTask {
        override fun cancel() {
            if (!this.bukkitTask.isCancelled) this.bukkitTask.cancel()
        }
    }
}
