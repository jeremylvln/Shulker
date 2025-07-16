package io.shulkermc.minestomdemo

import io.shulkermc.server.minestom.ShulkerServerAgentMinestom
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.block.Block
import java.util.logging.Logger

object MinestomDemo {
    @JvmStatic
    fun main(args: Array<String>) {
        ShulkerServerAgentMinestom.init(Logger.getLogger("MinestomDemo"))

        val instanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer = instanceManager.createInstanceContainer()
        instanceContainer.setGenerator { unit ->
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK)
        }

        val globalEventHandler = MinecraftServer.getGlobalEventHandler()
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player = event.player
            event.spawningInstance = instanceContainer
            player.respawnPoint = Pos(0.0, 42.0, 0.0)
        }

        ShulkerServerAgentMinestom.start()
    }
}
