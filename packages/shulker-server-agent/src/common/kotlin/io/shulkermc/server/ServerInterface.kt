package io.shulkermc.server

import io.shulkermc.server.platform.HookPostOrder
import io.shulkermc.server.platform.PlayerDisconnectHook
import io.shulkermc.server.platform.PlayerLoginHook
import java.util.UUID
import java.util.concurrent.TimeUnit

interface ServerInterface {
    fun prepareNetworkAdminsPermissions(playerIds: List<UUID>)

    fun addPlayerJoinHook(
        hook: PlayerLoginHook,
        postOrder: HookPostOrder,
    )

    fun addPlayerQuitHook(
        hook: PlayerDisconnectHook,
        postOrder: HookPostOrder,
    )

    fun getPlayerCount(): Int

    fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ScheduledTask

    fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ScheduledTask

    interface ScheduledTask {
        fun cancel()
    }
}
