package io.shulkermc.server.tasks

import io.shulkermc.server.ServerInterface
import io.shulkermc.server.ShulkerServerAgentCommon
import java.util.concurrent.TimeUnit

class HealthcheckTask(private val agent: ShulkerServerAgentCommon) : Runnable {
    companion object {
        private const val HEALTHCHECK_INTERVAL_SECONDS = 5L
    }

    fun schedule(): ServerInterface.ScheduledTask {
        return this.agent.serverInterface.scheduleRepeatingTask(
            0L,
            HEALTHCHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS,
            this,
        )
    }

    override fun run() {
        this.agent.cluster.agonesGateway.sendHealthcheck()
    }
}
