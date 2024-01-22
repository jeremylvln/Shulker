package io.shulkermc.serveragent.services

import io.shulkermc.serveragent.Configuration
import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serveragent.platform.HookPostOrder

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
            this.agent.agonesGateway.setAllocated()
        } else if (!triggerFromJoin && playerCount == 1) {
            this.agent.agonesGateway.setReady()
        }
    }
}
