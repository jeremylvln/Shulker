package io.shulkermc.server.api;

import java.util.concurrent.CompletableFuture;

public abstract class ShulkerServerAPI {
    private static ShulkerServerAPI INSTANCE;

    ShulkerServerAPI() {
        INSTANCE = this;
    }

    public static ShulkerServerAPI instance() {
        return INSTANCE;
    }

    abstract public void askShutdown();
    abstract public CompletableFuture<Void> setReady();
    abstract public CompletableFuture<Void> setAllocated();
    abstract public CompletableFuture<Void> setReserved(long seconds);
}
