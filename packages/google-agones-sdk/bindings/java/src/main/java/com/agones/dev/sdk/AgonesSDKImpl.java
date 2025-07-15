package com.agones.dev.sdk;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class AgonesSDKImpl implements AgonesSDK {
    private static final Empty EMPTY_PAYLOAD = Empty.getDefaultInstance();
    private static final StreamObserver<Empty> EMPTY_STREAM_OBSERVER = new StreamObserver<>() {
        @Override
        public void onNext(Empty value) {}

        @Override
        public void onError(Throwable t) {}

        @Override
        public void onCompleted() {}
    };

    private final ManagedChannel channel;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SDKGrpc.SDKStub stub;
    private final SDKGrpc.SDKFutureStub asyncStub;

    private final StreamObserver<Empty> healthcheckObserver;

    private final Alpha alphaSdk;

    private AgonesSDKImpl(ManagedChannel channel) {
        this.channel = channel;
        this.stub = SDKGrpc.newStub(channel);
        this.asyncStub = SDKGrpc.newFutureStub(channel);
        this.alphaSdk = new AlphaImpl();

        this.healthcheckObserver = this.stub.health(EMPTY_STREAM_OBSERVER);
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
    public void watchGameServer(Consumer<GameServer> consumer) {
        // Create a StreamObserver that forwards each GameServer to the Consumer
        StreamObserver<GameServer> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(GameServer value) {
                // Forward the GameServer to the Consumer for handling
                consumer.accept(value);
            }

            @Override
            public void onError(Throwable t) {
                // Optionally handle errors
                System.err.println("Error during stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // Optionally handle completion of the stream
                System.out.println("Stream completed.");
            }
        };

        // Make the gRPC call to start watching the GameServer stream
        this.stub.watchGameServer(EMPTY_PAYLOAD, responseObserver);
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
        private static final com.agones.dev.sdk.alpha.Empty EMPTY_PAYLOAD = com.agones.dev.sdk.alpha.Empty.getDefaultInstance();

        private final com.agones.dev.sdk.alpha.SDKGrpc.SDKFutureStub asyncStub = com.agones.dev.sdk.alpha.SDKGrpc.newFutureStub(channel);

        @Override
        public CompletableFuture<Boolean> notifyPlayerConnected(String id) {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply(com.agones.dev.sdk.alpha.Bool::getBool);
        }

        @Override
        public CompletableFuture<Boolean> notifyPlayerDisconnected(String id) {
            return wrapGrpcFuture(this.asyncStub.playerConnect(this.createPlayerId(id))).thenApply(com.agones.dev.sdk.alpha.Bool::getBool);
        }

        @Override
        public CompletableFuture<Void> setPlayerCapacity(long capacity) {
            return wrapGrpcFuture(
                this.asyncStub.setPlayerCapacity(com.agones.dev.sdk.alpha.Count.newBuilder().setCount(capacity).build())
            ).thenAccept((reply) -> {});
        }

        @Override
        public CompletableFuture<Long> getPlayerCapacity() {
            return wrapGrpcFuture(this.asyncStub.getPlayerCapacity(EMPTY_PAYLOAD)).thenApply(com.agones.dev.sdk.alpha.Count::getCount);
        }

        @Override
        public CompletableFuture<Long> getPlayerCount() {
            return wrapGrpcFuture(this.asyncStub.getPlayerCount(EMPTY_PAYLOAD)).thenApply(com.agones.dev.sdk.alpha.Count::getCount);
        }

        @Override
        public CompletableFuture<Boolean> isPlayerConnected(String id) {
            return wrapGrpcFuture(this.asyncStub.isPlayerConnected(this.createPlayerId(id))).thenApply(com.agones.dev.sdk.alpha.Bool::getBool);
        }

        @Override
        public CompletableFuture<List<String>> getConnectedPlayers() {
            return wrapGrpcFuture(this.asyncStub.getConnectedPlayers(EMPTY_PAYLOAD)).thenApply(com.agones.dev.sdk.alpha.PlayerIDList::getListList);
        }

        private com.agones.dev.sdk.alpha.PlayerID createPlayerId(String id) {
            return com.agones.dev.sdk.alpha.PlayerID.newBuilder().setPlayerID(id).build();
        }
    }
}
