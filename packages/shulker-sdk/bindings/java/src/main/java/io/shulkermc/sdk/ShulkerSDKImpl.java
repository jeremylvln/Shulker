package io.shulkermc.sdk;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.shulkermc.sdk.v1alpha1.MinecraftServerFleetServiceGrpc;
import io.shulkermc.sdk.v1alpha1.SummonFromFleetReply;
import io.shulkermc.sdk.v1alpha1.SummonFromFleetRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ShulkerSDKImpl implements ShulkerSDK {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MinecraftServerFleetServiceGrpc.MinecraftServerFleetServiceFutureStub minecraftServerFleetServiceFutureStub;

    private ShulkerSDKImpl(ManagedChannel channel) {
        this.minecraftServerFleetServiceFutureStub = MinecraftServerFleetServiceGrpc.newFutureStub(channel);
    }

    @Override
    public CompletableFuture<String> summonFromFleet(String namespace, String fleetName) {
        return this.wrapGrpcFuture(this.minecraftServerFleetServiceFutureStub.summonFromFleet(SummonFromFleetRequest.newBuilder()
                .setNamespace(namespace)
                .setName(fleetName)
                .build()
        )).thenApply(SummonFromFleetReply::getGameServerId);
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

    public static ShulkerSDK createFromEnvironment() {
        String hostString = System.getenv("SHULKER_SDK_GRPC_HOST");
        if (hostString == null) {
            throw new IllegalStateException("Missing SHULKER_SDK_GRPC_HOST environment variable");
        }

        String portString = System.getenv("SHULKER_SDK_GRPC_PORT");
        if (portString == null) {
            throw new IllegalStateException("Missing SHULKER_SDK_GRPC_PORT environment variable");
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostString, Integer.parseInt(portString)).usePlaintext().build();
        return new ShulkerSDKImpl(channel);
    }
}
