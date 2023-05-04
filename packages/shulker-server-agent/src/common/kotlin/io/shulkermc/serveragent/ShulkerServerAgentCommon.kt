package io.shulkermc.serveragent

import io.shulkermc.serveragent.adapters.agones.AgonesGatewayAdapter
import io.shulkermc.serveragent.adapters.agones.AgonesGatewayAdapterImpl
import io.shulkermc.serveragent.api.ShulkerServerAPIImpl
import java.lang.Exception
import java.util.logging.Logger

class ShulkerServerAgentCommon(private val logger: Logger) {
    var agonesGateway: AgonesGatewayAdapter? = null

    fun onServerInitialization() {
        try {
            val config = Configuration.load()
            ShulkerServerAPIImpl(this)

            this.logger.info("Identified Shulker server: ${config.serverNamespace}/${config.serverName}")
            this.agonesGateway = AgonesGatewayAdapterImpl()

            this.agonesGateway!!.emitServerReady()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
        }
    }

    fun onServerShutdown() {
        if (this.agonesGateway != null) {
            this.agonesGateway!!.emitServerShutdown()
            this.agonesGateway!!.destroy()
        }
    }
}
