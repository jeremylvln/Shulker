package io.shulkermc.cluster.api.messaging;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MessagingBus {
    void subscribe(@NotNull String channel, @NotNull Consumer<String> callback);

    void broadcastToAllProxies(@NotNull String channel, @NotNull String message);
    void sendToProxy(@NotNull String proxyName, @NotNull String channel, @NotNull String message);

    void broadcastToAllServers(@NotNull String channel, @NotNull String message);
    void sendToServer(@NotNull String serverName, @NotNull String channel, @NotNull String message);
}
