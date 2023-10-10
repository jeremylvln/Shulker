package dev.agones;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dev.agones.dev.sdk.Duration;
import dev.agones.dev.sdk.Empty;
import dev.agones.dev.sdk.GameServer;
import dev.agones.dev.sdk.SDKGrpc;;
import dev.agones.dev.sdk.alpha.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AgonesSDKImpl implements AgonesSDK {
    private static final Empty EMPTY_PAYLOAD = Empty.getDefaultInstance();

    private final ManagedChannel channel;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SDKGrpc.SDKFutureStub asyncStub;
    private final StreamObserver<Empty> healthcheckObserver = new StreamObserver<Empty>() {
        @Override
        public void onNext(Empty value) {}

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onCompleted() {}
    };

    private final Alpha alphaSdk = new AlphaImpl();

    private AgonesSDKImpl(ManagedChannel channel) {
        this.channel = channel;
        this.asyncStub = SDKGrpc.newFutureStub(channel);
    }

    @Override
    public void destroy() {
        this.channel.shutdownNow();
    }

    @Override
    public CompletableFuture<GameServer> getGameServer() {
        return this.wrapGrpcFuture(this.asyncStub.getGameServer(EMPTY_PAYLOAD));
    }

    @Override
    public CompletableFuture<String> getState() {
        return this.getGameServer().thenApply((gameServer) -> gameServer.getStatus().getState());
    }

    @Override
    public CompletableFuture<Void> setReady() {
        return this.wrapGrpcFuture(this.asyncStub.ready(EMPTY_PAYLOAD)).thenAccept((reply) -> {});
    }

    @Override
    public CompletableFuture<Void> setAllocated() {
        return this.wrapGrpcFuture(this.asyncStub.allocate(EMPTY_PAYLOAD)).thenAccept((reply) -> {});
    }

    @Override
    public CompletableFuture<Void> setReserved(long seconds) {
        return this.wrapGrpcFuture(
            this.asyncStub.reserve(Duration.newBuilder().setSeconds(seconds).build())
        ).thenAccept((reply) -> {});
    }

    @Override
    public void askShutdown() {
        try {
            this.wrapGrpcFuture(
                    this.asyncStub.shutdown(EMPTY_PAYLOAD)
            ).get();
        } catch (Exception ignored) {}
    }

    @Override
    public void sendHealthcheck() {
        this.healthcheckObserver.onNext(EMPTY_PAYLOAD);
    }

    private <T> CompletableFuture<T> wrapGrpcFuture(ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> future = new CompletableFuture<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(
            listenableFuture,
            new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    future.complete(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    future.completeExceptionally(t);
                }
            },
            this.executor
        );

        return future;
    }

    @Override
    public Alpha alpha() {
        return this.alphaSdk;
    }

    public static AgonesSDK createFromEnvironment() {
        String portString = System.getenv("AGONES_SDK_GRPC_PORT");
        if (portString == null) {
            throw new IllegalStateException("Missing AGONES_SDK_GRPC_PORT environment variable");
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", Integer.parseInt(portString)).usePlaintext().build();
        return new AgonesSDKImpl(channel);
    }

    public final class AlphaImpl implements AgonesSDK.Alpha {
        private static final dev.agones.dev.sdk.alpha.Empty EMPTY_PAYLOAD = dev.agones.dev.sdk.alpha.Empty.getDefaultInstance();

        private final dev.agones.dev.sdk.alpha.SDKGrpc.SDKFutureStub asyncStub = dev.agones.dev.sdk.alpha.SDKGrpc.newFutureStub(channel);

        @Override
        public CompletableFuture<Boolean> notifyPlayerConnected(String id) {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply(Bool::getBool);
        }

        @Override
        public CompletableFuture<Boolean> notifyPlayerDisconnected(String id) {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply(Bool::getBool);
        }

        @Override
        public CompletableFuture<Void> setPlayerCapacity(long capacity) {
            return wrapGrpcFuture(
                this.asyncStub.setPlayerCapacity(Count.newBuilder().setCount(capacity).build())
            ).thenAccept((reply) -> {});
        }

        @Override
        public CompletableFuture<Long> getPlayerCapacity() {
            return wrapGrpcFuture(this.asyncStub.getPlayerCapacity(EMPTY_PAYLOAD)).thenApply(Count::getCount);
        }

        @Override
        public CompletableFuture<Long> getPlayerCount() {
            return wrapGrpcFuture(this.asyncStub.getPlayerCount(EMPTY_PAYLOAD)).thenApply(Count::getCount);
        }

        @Override
        public CompletableFuture<Boolean> isPlayerConnected(String id) {
            return wrapGrpcFuture(this.asyncStub.isPlayerConnected(this.createPlayerId(id))).thenApply(Bool::getBool);
        }

        @Override
        public CompletableFuture<List<String>> getConnectedPlayers() {
            return wrapGrpcFuture(this.asyncStub.getConnectedPlayers(EMPTY_PAYLOAD)).thenApply(PlayerIDList::getListList);
        }

        private PlayerID createPlayerId(String id) {
            return PlayerID.newBuilder().setPlayerID(id).build();
        }
    }
}
