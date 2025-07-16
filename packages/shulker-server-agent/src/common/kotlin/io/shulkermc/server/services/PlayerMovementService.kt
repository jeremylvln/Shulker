package io.shulkermc.server.services

import io.shulkermc.server.Configuration
import io.shulkermc.server.ShulkerServerAgentCommon
import io.shulkermc.server.platform.HookPostOrder

class PlayerMovementService(private val agent: ShulkerServerAgentCommon) {
    init {
        this.agent.serverInterface.addPlayerJoinHook(this::onPlayerJoin, HookPostOrder.MONITOR)
        this.agent.serverInterface.addPlayerQuitHook(this::onPlayerQuit, HookPostOrder.MONITOR)
    }

    private fun onPlayerJoin() {
        this.updateAllocationState(triggerFromJoin = true)
    }

    private fun onPlayerQuit() {
        this.updateAllocationState(triggerFromJoin = false)
    }

    private fun updateAllocationState(triggerFromJoin: Boolean) {
        if (Configuration.LIFECYCLE_STRATEGY !== Configuration.LifecycleStrategy.ALLOCATE_WHEN_NOT_EMPTY) {
            return
        }

        val playerCount = this.agent.serverInterface.getPlayerCount()

        if (triggerFromJoin && playerCount == 1) {
            this.agent.cluster.agonesGateway.setAllocated()
        } else if (!triggerFromJoin && playerCount == 1) {
            this.agent.cluster.agonesGateway.setReady()
        }
    }
}
