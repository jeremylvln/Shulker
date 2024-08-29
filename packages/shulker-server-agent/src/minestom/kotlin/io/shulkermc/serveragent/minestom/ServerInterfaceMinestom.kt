package io.shulkermc.serveragent.minestom

import io.shulkermc.serveragent.ServerInterface
import io.shulkermc.serveragent.platform.HookPostOrder
import io.shulkermc.serveragent.platform.PlayerDisconnectHook
import io.shulkermc.serveragent.platform.PlayerLoginHook
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.permission.Permission
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

class ServerInterfaceMinestom : ServerInterface {
    companion object {
        private const val ADMIN_PERMISSION_LEVEL = 4
    }

    private val eventNode = EventNode.all("shulker-server-agent-minestom")

    override fun prepareNetworkAdminsPermissions(playerIds: List<UUID>) {
        this.eventNode.addListener(PlayerSpawnEvent::class.java) { event: PlayerSpawnEvent ->
            if (playerIds.contains(event.player.uuid)) {
                event.player.permissionLevel = ADMIN_PERMISSION_LEVEL
                event.player.addPermission(Permission("*"))
            }
        }
    }

    override fun addPlayerJoinHook(
        hook: PlayerLoginHook,
        postOrder: HookPostOrder,
    ) {
        this.eventNode.addListener(PlayerSpawnEvent::class.java) { _ -> hook() }
    }

    override fun addPlayerQuitHook(
        hook: PlayerDisconnectHook,
        postOrder: HookPostOrder,
    ) {
        this.eventNode.addListener(PlayerDisconnectEvent::class.java) { _ -> hook() }
    }

    override fun getPlayerCount(): Int = MinecraftServer.getConnectionManager().onlinePlayers.size

    override fun scheduleDelayedTask(
        delay: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask {
        val duration = Duration.ofNanos(timeUnit.toNanos(delay))
        val task =
            MinecraftServer.getSchedulerManager().scheduleTask(
                runnable,
                TaskSchedule.duration(duration),
                TaskSchedule.stop(),
            )

        return MinestomScheduledTask(task)
    }

    override fun scheduleRepeatingTask(
        delay: Long,
        interval: Long,
        timeUnit: TimeUnit,
        runnable: Runnable,
    ): ServerInterface.ScheduledTask {
        val delayDuration = Duration.ofNanos(timeUnit.toNanos(delay))
        val intervalDuration = Duration.ofNanos(timeUnit.toNanos(interval))
        val task =
            MinecraftServer.getSchedulerManager().scheduleTask(
                runnable,
                TaskSchedule.duration(delayDuration),
                TaskSchedule.duration(intervalDuration),
            )

        return MinestomScheduledTask(task)
    }

    private class MinestomScheduledTask(private val minestomTask: Task) : ServerInterface.ScheduledTask {
        override fun cancel() {
            this.minestomTask.cancel()
        }
    }
}
