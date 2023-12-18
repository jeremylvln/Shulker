package io.shulkermc.serveragent.paper.scheduler

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import io.shulkermc.serveragent.ServerInterface
import io.shulkermc.serveragent.paper.ShulkerServerAgentPaper
import java.util.concurrent.TimeUnit

class ServerSchedulerFolia(private val plugin: ShulkerServerAgentPaper) : ServerScheduler {
    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask {
        return FoliaScheduledTask(
            this.plugin.server.asyncScheduler.runDelayed(this.plugin, { runnable.run() }, delay, timeUnit)
        )
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable
    ): ServerInterface.ScheduledTask {
        return FoliaScheduledTask(
            this.plugin.server.asyncScheduler.runAtFixedRate(this.plugin, {
                runnable.run()
            }, delay, interval, timeUnit)
        )
    }

    private class FoliaScheduledTask(private val scheduledTask: ScheduledTask) : ServerInterface.ScheduledTask {
        override fun cancel() {
            if (!this.scheduledTask.isCancelled) this.scheduledTask.cancel()
        }
    }
}
