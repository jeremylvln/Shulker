package io.shulkermc.server.minestom

import io.shulkermc.server.ShulkerServerAgentCommon
import io.shulkermc.server.minestom.config.PaperConfiguration
import io.shulkermc.server.minestom.config.ServerProperties
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
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

        @JvmStatic
        fun getInstance(): ShulkerServerAgentMinestom {
            return checkNotNull(INSTANCE) { "Shulker Agent has not been initialized yet" }
        }
    }

    private lateinit var server: MinecraftServer
    private lateinit var serverProperties: ServerProperties
    private lateinit var paperConfig: PaperConfiguration

    private val agent = ShulkerServerAgentCommon(ServerInterfaceMinestom(), this.logger)

    fun getConfiguredAuth(): Auth { // public so that anyone can check the configured auth type
        if (this.paperConfig.proxies.velocity.enabled) {
            return Auth.Velocity(this.paperConfig.proxies.velocity.secret!!)
        } else {
            return Auth.Bungee()
        }
    }

    private fun onServerInitialization() {
        if (System.getenv("EXEC_DIRECTLY") != "true") {
            this.logger.severe("Please set the environment variable EXEC_DIRECTLY to true. It is required so the Minestom server can be shutdown properly.")
            exitProcess(1)
        }

        this.logger.info("Loading configuration files")
        this.serverProperties = ServerProperties.load()
        this.paperConfig = PaperConfiguration.load()

        val auth = getConfiguredAuth()
        this.logger.info("Configured auth type: ${auth.javaClass.simpleName}")

        this.server = MinecraftServer.init(auth)

        this.agent.onServerInitialization()

        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    logger.info("Shutdown signal received, stopping cleanly")
                    MinecraftServer.stopCleanly()
                }
            },
        )
    }

    private fun onServerReady() {
        this.logger.info("Starting server on address ${this.serverProperties.serverIp}:${this.serverProperties.serverPort}")
        this.server.start(this.serverProperties.serverIp, this.serverProperties.serverPort)
    }

    private fun onServerShutdown() {
        this.agent.onServerShutdown()
    }
}
