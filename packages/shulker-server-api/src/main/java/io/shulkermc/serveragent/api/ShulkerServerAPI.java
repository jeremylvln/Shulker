package io.shulkermc.serveragent.api;

import com.agones.dev.sdk.GameServer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class ShulkerServerAPI {
    public static ShulkerServerAPI INSTANCE;

    abstract public void askShutdown();
    abstract public CompletableFuture<Void> setReady();
    abstract public void watchGameServer(Consumer<GameServer> consumer);
    abstract public CompletableFuture<Void> setAllocated();
    abstract public CompletableFuture<Void> setReserved(long seconds);
}
