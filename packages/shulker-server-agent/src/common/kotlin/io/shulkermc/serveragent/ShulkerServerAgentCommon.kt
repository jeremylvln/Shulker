package io.shulkermc.serveragent

import agones.dev.sdk.AgonesSDK
import agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.serveragent.api.ShulkerServerAPIImpl
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class ShulkerServerAgentCommon(private val serverInterface: ServerInterface, val logger: Logger) {
    companion object {
        private const val SUMMON_LABEL_NAME = "shulkermc.io/summoned"
        private const val SUMMON_TIMEOUT_MINUTES = 5L
    }

    lateinit var agonesGateway: AgonesSDK
    private lateinit var healthcheckTask: ServerInterface.ScheduledTask
    private var summonTimeoutTask: ServerInterface.ScheduledTask? = null

    fun onServerInitialization() {
        try {
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info("Identified Shulker server: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}")

            if (gameServer.objectMeta.containsLabels(SUMMON_LABEL_NAME)) {
                this.logger.info("This server was summoned manually, it will be shutdown automatically in $SUMMON_TIMEOUT_MINUTES minutes")
                this.summonTimeoutTask = this.serverInterface.scheduleDelayedTask(SUMMON_TIMEOUT_MINUTES, TimeUnit.MINUTES) {
                    this.agonesGateway.getState().thenAccept { state ->
                        if (state == "Ready") {
                            this.logger.info("Server still in Ready state after $SUMMON_TIMEOUT_MINUTES minutes, asking shutdown")
                            this.agonesGateway.askShutdown()
                        }
                    }
                }
            }

            ShulkerServerAPIImpl(this)

            this.healthcheckTask = this.serverInterface.scheduleRepeatingTask(0L, 10L, TimeUnit.SECONDS) {
                this.agonesGateway.sendHealthcheck()
            }

            this.agonesGateway.setReady()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
            this.agonesGateway.askShutdown()
        }
    }

    fun onServerShutdown() {
        this.summonTimeoutTask?.cancel()
        this.healthcheckTask.cancel()
        this.agonesGateway.askShutdown()
        this.agonesGateway.destroy()
    }
}
