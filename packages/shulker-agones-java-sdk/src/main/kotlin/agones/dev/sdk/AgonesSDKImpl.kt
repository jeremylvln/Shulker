package agones.dev.sdk

import agones.dev.sdk.Sdk.Duration
import agones.dev.sdk.Sdk.GameServer
import agones.dev.sdk.alpha.Sdk.PlayerID
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class AgonesSDKImpl private constructor(private val channel: ManagedChannel) : AgonesSDK {
    companion object {
        private val EMPTY_PAYLOAD = Sdk.Empty.newBuilder().build()
        private val EMPTY_ALPHA_PAYLOAD = agones.dev.sdk.alpha.Sdk.Empty.newBuilder().build()

        fun createFromEnvironment(): AgonesSDK {
            val channel = ManagedChannelBuilder.forAddress(
                "127.0.0.1",
                checkNotNull(System.getenv("AGONES_SDK_GRPC_PORT")) { "Missing AGONES_SDK_GRPC_PORT environment variable" }.toInt()
            ).usePlaintext().build()

            return AgonesSDKImpl(channel)
        }
    }

    private val executor = Executors.newCachedThreadPool()
    private val asyncStub = SDKGrpc.newFutureStub(this.channel)
    private val stub = SDKGrpc.newStub(this.channel)
    private val healthcheckObserver: StreamObserver<Sdk.Empty>

    override val alpha = AlphaImpl()

    init {
        this.healthcheckObserver = this.stub.health(object : StreamObserver<Sdk.Empty> {
            override fun onNext(value: Sdk.Empty) {}
            override fun onError(t: Throwable) { throw t }
            override fun onCompleted() {}
        })
    }

    override fun destroy() {
        this.channel.shutdownNow()
    }

    override fun getGameServer(): CompletableFuture<GameServer> {
        return this.wrapGrpcFuture(
            this.asyncStub.getGameServer(EMPTY_PAYLOAD)
        )
    }

    override fun getState(): CompletableFuture<String> {
        return this.getGameServer().thenApply { it.status.state }
    }

    override fun setReady(): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.ready(EMPTY_PAYLOAD)
        ).thenApply {}
    }

    override fun setAllocated(): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.allocate(EMPTY_PAYLOAD)
        ).thenApply {}
    }

    override fun setReserved(seconds: Long): CompletableFuture<Unit> {
        return this.wrapGrpcFuture(
            this.asyncStub.reserve(Duration.newBuilder().setSeconds(seconds).build())
        ).thenApply {}
    }

    override fun askShutdown() {
        this.wrapGrpcFuture(
            this.asyncStub.shutdown(EMPTY_PAYLOAD)
        ).get()
    }

    override fun sendHealthcheck() {
        this.healthcheckObserver.onNext(EMPTY_PAYLOAD)
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

    private fun <T> createStreamObserverForFuture(future: CompletableFuture<T>): StreamObserver<T> {
        return object : StreamObserver<T> {
            override fun onNext(value: T) {
                if (!future.isDone) future.complete(value)
            }
            override fun onError(t: Throwable) { future.completeExceptionally(t) }
            override fun onCompleted() {}
        }
    }

    inner class AlphaImpl : AgonesSDK.Alpha {
        private val asyncStub = agones.dev.sdk.alpha.SDKGrpc.newFutureStub(channel)

        override fun notifyPlayerConnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply { it.bool }
        }

        override fun notifyPlayerDisconnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(
                this.asyncStub.playerConnect(this.createPlayerId(id))
            ).thenApply { it.bool }
        }

        override fun setPlayerCapacity(capacity: Long): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.setPlayerCapacity(agones.dev.sdk.alpha.Sdk.Count.newBuilder().setCount(capacity).build())
            ).thenApply {}
        }

        override fun getPlayerCapacity(): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.getPlayerCapacity(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.count }
        }

        override fun getPlayerCount(): CompletableFuture<Unit> {
            return wrapGrpcFuture(
                this.asyncStub.getPlayerCount(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.count }
        }

        override fun isPlayerConnected(id: String): CompletableFuture<Boolean> {
            return wrapGrpcFuture(
                this.asyncStub.isPlayerConnected(this.createPlayerId(id))
            ).thenApply { it.bool }
        }

        override fun getConnectedPlayers(): CompletableFuture<List<String>> {
            return wrapGrpcFuture(
                this.asyncStub.getConnectedPlayers(EMPTY_ALPHA_PAYLOAD)
            ).thenApply { it.listList }
        }

        private fun createPlayerId(id: String): PlayerID = PlayerID.newBuilder()
            .setPlayerID(id)
            .build()
    }
}
