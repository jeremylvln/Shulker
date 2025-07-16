package io.shulkermc.server.paper.scheduler

import io.shulkermc.server.ServerInterface
import java.util.concurrent.TimeUnit

interface ServerScheduler {
    fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask

    fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask
}
