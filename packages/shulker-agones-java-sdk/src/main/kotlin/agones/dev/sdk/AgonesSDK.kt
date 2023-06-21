package agones.dev.sdk

import agones.dev.sdk.Sdk.GameServer
import java.util.concurrent.CompletableFuture

interface AgonesSDK {
    val alpha: AgonesSDK.Alpha

    fun destroy()

    fun getGameServer(): CompletableFuture<GameServer>
    fun getState(): CompletableFuture<String>

    fun setReady(): CompletableFuture<Unit>
    fun setAllocated(): CompletableFuture<Unit>
    fun setReserved(seconds: Long): CompletableFuture<Unit>
    fun askShutdown()

    fun sendHealthcheck()

    interface Alpha {
        fun notifyPlayerConnected(id: String): CompletableFuture<Boolean>
        fun notifyPlayerDisconnected(id: String): CompletableFuture<Boolean>
        fun setPlayerCapacity(capacity: Long): CompletableFuture<Unit>
        fun getPlayerCapacity(): CompletableFuture<Unit>
        fun getPlayerCount(): CompletableFuture<Unit>
        fun isPlayerConnected(id: String): CompletableFuture<Boolean>
        fun getConnectedPlayers(): CompletableFuture<List<String>>
    }
}
