package io.shulkermc.serveragent.paper.scheduler

import io.shulkermc.serveragent.ServerInterface
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
