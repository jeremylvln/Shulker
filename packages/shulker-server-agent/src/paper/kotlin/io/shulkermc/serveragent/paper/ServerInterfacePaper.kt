package io.shulkermc.serveragent.paper

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import io.shulkermc.serveragent.ServerInterface
import java.util.concurrent.TimeUnit

class ServerInterfacePaper(private val plugin: ShulkerServerAgentPaper) : ServerInterface {
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
