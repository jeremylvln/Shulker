package com.agones.dev.sdk;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AgonesSDK {
    void destroy();

    CompletableFuture<GameServer> getGameServer();
    CompletableFuture<String> getState();

    CompletableFuture<Void> setReady();
    CompletableFuture<Void> setAllocated();
    CompletableFuture<Void> setReserved(long seconds);
    void askShutdown();

    void sendHealthcheck();

    Alpha alpha();
    interface Alpha {
        CompletableFuture<Boolean> notifyPlayerConnected(String id);
        CompletableFuture<Boolean> notifyPlayerDisconnected(String id);
        CompletableFuture<Void> setPlayerCapacity(long capacity);
        CompletableFuture<Long> getPlayerCapacity();
        CompletableFuture<Long> getPlayerCount();
        CompletableFuture<Boolean> isPlayerConnected(String id);
        CompletableFuture<List<String>> getConnectedPlayers();
    }
}
