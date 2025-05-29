package io.shulkermc.serveragent.minestom

import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serveragent.minestom.config.PaperConfiguration
import io.shulkermc.serveragent.minestom.config.ServerProperties
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.bungee.BungeeCordProxy
import net.minestom.server.extras.velocity.VelocityProxy
import java.util.logging.Logger
import kotlin.system.exitProcess

@Suppress("unused")
class ShulkerServerAgentMinestom private constructor(private val logger: Logger) {
    companion object {
        @Suppress("ktlint:standard:property-naming")
        private var INSTANCE: ShulkerServerAgentMinestom? = null

        @JvmStatic
        fun init(logger: Logger) {
            if (INSTANCE != null) {
                logger.warning("Tried to call initialize the Shulker Agent twice, ignoring")
                return
            }

            val instance = ShulkerServerAgentMinestom(logger)
            instance.onServerInitialization()
            MinecraftServer.getSchedulerManager().buildShutdownTask(instance::onServerShutdown)

            INSTANCE = instance
        }

        @JvmStatic
        fun start() {
            checkNotNull(INSTANCE) { "Shulker Agent should have been initialized first" }
            INSTANCE!!.onServerReady()
        }
    }

    private lateinit var server: MinecraftServer
    private lateinit var serverProperties: ServerProperties
    private lateinit var paperConfig: PaperConfiguration

    private val agent = ShulkerServerAgentCommon(ServerInterfaceMinestom(), this.logger)

    private fun onServerInitialization() {
        if (System.getenv("EXEC_DIRECTLY") != "true") {
            this.logger.severe("Please set the environment variable EXEC_DIRECTLY to true. It is required so the Minestom server can be shutdown properly.")
            exitProcess(1)
        }

        this.server = MinecraftServer.init()

        this.logger.info("Loading configuration files")
        this.serverProperties = ServerProperties.load()
        this.paperConfig = PaperConfiguration.load()

        if (this.paperConfig.proxies.velocity.enabled) {
            this.logger.info("Enabling Velocity middleware")
            VelocityProxy.enable(this.paperConfig.proxies.velocity.secret!!)
        } else {
            this.logger.info("Enabling BungeeCord middleware")
            BungeeCordProxy.enable()
        }

        this.agent.onServerInitialization()

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                logger.info("Shutdown signal received, stopping cleanly")
                MinecraftServer.stopCleanly()
            }
        })
    }

    private fun onServerReady() {
        this.logger.info("Starting server on address ${this.serverProperties.serverIp}:${this.serverProperties.serverPort}")
        this.server.start(this.serverProperties.serverIp, this.serverProperties.serverPort)
    }

    private fun onServerShutdown() {
        this.agent.onServerShutdown()
    }
}
