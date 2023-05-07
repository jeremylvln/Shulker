package agones.dev.sdk

import agones.dev.sdk.alpha.Sdk.PlayerID
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class AgonesSDK private constructor(private val channel: ManagedChannel) {
    companion object {
        private val EMPTY_PAYLOAD = Sdk.Empty.newBuilder().build()
        private val EMPTY_ALPHA_PAYLOAD = agones.dev.sdk.alpha.Sdk.Empty.newBuilder().build()

        fun createFromEnvironment(): AgonesSDK {
            val channel = ManagedChannelBuilder.forAddress(
                "127.0.0.1",
                checkNotNull(System.getenv("AGONES_SDK_GRPC_PORT")) { "Missing AGONES_SDK_GRPC_PORT environment variable" }.toInt()
            ).usePlaintext().build()

            return AgonesSDK(channel)
        }
    }

    private val executor = Executors.newCachedThreadPool()
    private val asyncStub = SDKGrpc.newFutureStub(this.channel)

    val alpha = Alpha()

    fun destroy() {
        this.channel.shutdownNow()
    }

    fun emitReady(): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.ready(EMPTY_PAYLOAD)
        ).thenApply {}
    }

    fun emitAllocated(): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.allocate(EMPTY_PAYLOAD)
        ).thenApply {}
    }

    fun emitShutdown(): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.shutdown(EMPTY_PAYLOAD)
        ).thenApply {}
    }

    private fun <T> wrapGrpcFuture(listenableFuture: ListenableFuture<T>): CompletableFuture<T> {
        val future = object : CompletableFuture<T>() {
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                val cancelled = listenableFuture.cancel(mayInterruptIfRunning)
                super.cancel(cancelled)
                return cancelled
            }
        }

        Futures.addCallback(
            listenableFuture,
            object : FutureCallback<T> {
                override fun onSuccess(result: T) { future.complete(result) }
                override fun onFailure(t: Throwable) { future.completeExceptionally(t) }
            },
            this.executor
        )

        return future
    }

    inner class Alpha {
        private val asyncStub = agones.dev.sdk.alpha.SDKGrpc.newFutureStub(channel)

        fun emitPlayerConnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply { it.bool }
        }

        fun emitPlayerDisconnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(
                this.asyncStub.playerConnect(this.createPlayerId(id))
            ).thenApply { it.bool }
        }

        fun setPlayerCapacity(capacity: Long): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.setPlayerCapacity(agones.dev.sdk.alpha.Sdk.Count.newBuilder().setCount(capacity).build())
            ).thenApply {}
        }

        fun getPlayerCapacity(): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.getPlayerCapacity(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.count }
        }

        fun getPlayerCount(): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.getPlayerCount(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.count }
        }

        fun isPlayerConnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(
                this.asyncStub.isPlayerConnected(this.createPlayerId(id))
            ).thenApply { it.bool }
        }

        fun getConnectedPlayers(): CompletableFuture<List<String>> {
            return wrapGrpcFuture(
                this.asyncStub.getConnectedPlayers(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.listList }
        }

        private fun createPlayerId(id: String): PlayerID = PlayerID.newBuilder()
            .setPlayerID(id)
            .build()
    }
}
