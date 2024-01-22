package io.shulkermc.serveragent.services

import io.shulkermc.serveragent.Configuration
import io.shulkermc.serveragent.ShulkerServerAgentCommon
import io.shulkermc.serveragent.platform.HookPostOrder
import io.shulkermc.serveragent.platform.Player

class PlayerMovementService(private val agent: ShulkerServerAgentCommon) {
    init {
        this.agent.serverInterface.addPlayerLoginHook(this::onPlayerLogin, HookPostOrder.MONITOR)
        this.agent.serverInterface.addPlayerDisconnectHook(this::onPlayerDisconnect, HookPostOrder.MONITOR)
    }

    private fun onPlayerLogin(@Suppress("UNUSED_PARAMETER") player: Player) {
        this.updateAllocationState()
    }

    private fun onPlayerDisconnect(@Suppress("UNUSED_PARAMETER") player: Player) {
        this.updateAllocationState()
    }

    private fun updateAllocationState() {
        if (Configuration.LIFECYCLE_STRATEGY !== Configuration.LifecycleStrategy.ALLOCATE_WHEN_NOT_EMPTY) {
            return
        }

        if (this.agent.serverInterface.getPlayerCount() > 0) {
            this.agent.agonesGateway.setAllocated()
        } else {
            this.agent.agonesGateway.setReady()
        }
    }
}
