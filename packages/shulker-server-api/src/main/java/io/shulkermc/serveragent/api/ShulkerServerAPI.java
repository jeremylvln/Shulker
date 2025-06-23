package io.shulkermc.serveragent.api;

import io.shulkermc.agent.api.ShulkerAPI;

import java.util.concurrent.CompletableFuture;

public abstract class ShulkerServerAPI implements ShulkerAPI {
    public static ShulkerServerAPI INSTANCE;

    abstract public void askShutdown();
    abstract public CompletableFuture<Void> setReady();
    abstract public CompletableFuture<Void> setAllocated();
    abstract public CompletableFuture<Void> setReserved(long seconds);
}
