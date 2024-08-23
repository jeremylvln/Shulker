package io.shulkermc.serveragent.paper.scheduler

import io.shulkermc.serveragent.ServerInterface
import io.shulkermc.serveragent.paper.ShulkerServerAgentPaper
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

class ServerSchedulerPaper(private val plugin: ShulkerServerAgentPaper) : ServerScheduler {
    companion object {
        private const val TICKS_PER_SECOND = 20L

        private fun timeUnitToTicks(
            value: Long,
            timeUnit: TimeUnit,
        ): Long = timeUnit.toSeconds(value) * TICKS_PER_SECOND
    }

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask {
        return PaperBukkitTask(
            this.plugin.server.scheduler.runTaskLaterAsynchronously(
                this.plugin,
                Runnable {
                    runnable.run()
                },
                timeUnitToTicks(delay, timeUnit),
            ),
        )
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask {
        return PaperBukkitTask(
            this.plugin.server.scheduler.runTaskTimerAsynchronously(
                this.plugin,
                Runnable {
                    runnable.run()
                },
                timeUnitToTicks(delay, timeUnit),
                timeUnitToTicks(interval, timeUnit),
            ),
        )
    }

    private class PaperBukkitTask(private val bukkitTask: BukkitTask) : ServerInterface.ScheduledTask {
        override fun cancel() {
            if (!this.bukkitTask.isCancelled) this.bukkitTask.cancel()
        }
    }
}
