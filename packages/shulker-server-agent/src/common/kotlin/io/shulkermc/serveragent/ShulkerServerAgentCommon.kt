package io.shulkermc.serveragent

import com.agones.dev.sdk.AgonesSDK
import com.agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.serveragent.api.ShulkerServerAPI
import io.shulkermc.serveragent.api.ShulkerServerAPIImpl
import io.shulkermc.serveragent.services.PlayerMovementService
import io.shulkermc.serveragent.tasks.HealthcheckTask
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerServerAgentCommon(val serverInterface: ServerInterface, val logger: Logger) {
    companion object {
        private const val SUMMON_LABEL_NAME = "shulkermc.io/summoned"
        private const val SUMMON_TIMEOUT_MINUTES = 5L
    }

    lateinit var agonesGateway: AgonesSDK

    // Services
    lateinit var playerMovementService: PlayerMovementService

    // Tasks
    private lateinit var healthcheckTask: ServerInterface.ScheduledTask
    private var summonTimeoutTask: ServerInterface.ScheduledTask? = null

    fun onServerInitialization() {
        try {
            this.logger.fine("Creating Agones SDK from environment")
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info(
                "Identified Shulker server: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}",
            )

            ShulkerServerAPI.INSTANCE = ShulkerServerAPIImpl(this)

            this.playerMovementService = PlayerMovementService(this)

            this.healthcheckTask = HealthcheckTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.serverInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators",
                )
            }

            if (gameServer.objectMeta.containsLabels(SUMMON_LABEL_NAME)) {
                this.logger.info(
                    "This server was summoned manually, it will be shutdown automatically in $SUMMON_TIMEOUT_MINUTES minutes",
                )
                this.summonTimeoutTask = this.createSummonTimeoutTask()
            }

            this.agonesGateway.setReady()
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

        try {
            this.agonesGateway.askShutdown()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(
                Level.SEVERE,
                "Failed to ask Agones sidecar to shutdown properly, stopping process manually",
                e,
            )

            exitProcess(0)
        }
    }

    private fun createSummonTimeoutTask() =
        this.serverInterface.scheduleDelayedTask(
            SUMMON_TIMEOUT_MINUTES,
            TimeUnit.MINUTES,
        ) {
            this.agonesGateway.getState().thenAccept { state ->
                if (state == "Ready") {
                    this.logger.info(
                        "Server still in Ready state after $SUMMON_TIMEOUT_MINUTES minutes, asking shutdown",
                    )
                    this.agonesGateway.askShutdown()
                }
            }
        }
}
