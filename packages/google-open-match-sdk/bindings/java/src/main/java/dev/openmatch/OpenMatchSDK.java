package dev.openmatch;

import dev.agones.dev.sdk.GameServer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OpenMatchSDK {



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
