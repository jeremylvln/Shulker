package io.shulkermc.serveragent

import java.util.concurrent.TimeUnit

interface ServerInterface {
    fun scheduleDelayedTask(delay: Long, timeUnit: TimeUnit, runnable: Runnable): ScheduledTask
    fun scheduleRepeatingTask(delay: Long, interval: Long, timeUnit: TimeUnit, runnable: Runnable): ScheduledTask

    interface ScheduledTask {
        fun cancel()
    }
}
