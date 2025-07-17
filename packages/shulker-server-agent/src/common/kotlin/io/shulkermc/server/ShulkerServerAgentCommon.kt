package io.shulkermc.server

import io.shulkermc.cluster.api.ShulkerClusterAPIImpl
import io.shulkermc.server.api.ShulkerServerAPIImpl
import io.shulkermc.server.services.PlayerMovementService
import io.shulkermc.server.tasks.HealthcheckTask
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class ShulkerServerAgentCommon(val serverInterface: ServerInterface, val logger: Logger) {
    companion object {
        private const val SUMMON_LABEL_NAME = "shulkermc.io/summoned"
        private const val SUMMON_TIMEOUT_MINUTES = 5L
    }

    lateinit var cluster: ShulkerClusterAPIImpl
    lateinit var api: ShulkerServerAPIImpl

    // Services
    lateinit var playerMovementService: PlayerMovementService

    // Tasks
    private lateinit var healthcheckTask: ServerInterface.ScheduledTask
    private var summonTimeoutTask: ServerInterface.ScheduledTask? = null

    fun onServerInitialization() {
        this.logger.info("Agent version ${BuildConfig.VERSION} built on ${BuildConfig.BUILD_TIME}")

        try {
            this.cluster = ShulkerClusterAPIImpl(this.logger)
            this.api = ShulkerServerAPIImpl(this)

            this.playerMovementService = PlayerMovementService(this)

            this.healthcheckTask = HealthcheckTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.serverInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators",
                )
            }

            if (this.cluster.selfGameServer.objectMeta.containsLabels(SUMMON_LABEL_NAME)) {
                this.logger.info(
                    "This server was summoned manually, it will be shutdown automatically in $SUMMON_TIMEOUT_MINUTES minutes",
                )
                this.summonTimeoutTask = this.createSummonTimeoutTask()
            }

            this.cluster.agonesGateway.setReady()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Shulker Agent crashed, stopping server", e)
            this.shutdown()
        }
    }

    fun onServerShutdown() {
        this.shutdown()
    }

    fun shutdown() {
        try {
            this.summonTimeoutTask?.cancel()

            if (this::healthcheckTask.isInitialized) {
                this.healthcheckTask.cancel()
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Failed to properly terminate services", e)
        }

        this.cluster.close()
    }

    private fun createSummonTimeoutTask() =
        this.serverInterface.scheduleDelayedTask(
            SUMMON_TIMEOUT_MINUTES,
            TimeUnit.MINUTES,
        ) {
            this.cluster.agonesGateway.getState().thenAccept { state ->
                if (state == "Ready") {
                    this.logger.info("Server still in Ready state after $SUMMON_TIMEOUT_MINUTES minutes, asking shutdown")
                    this.cluster.agonesGateway.askShutdown()
                }
            }
        }
}
