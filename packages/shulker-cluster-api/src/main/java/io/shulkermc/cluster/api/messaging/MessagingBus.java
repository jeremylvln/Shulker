package io.shulkermc.cluster.api.messaging;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Straightforward pub-sub interface that can be used to communicate
 * with any component connected to the Shulker cluster. This may
 * include any proxy or server running a Shulker agent.
 * <br>
 * One is free from using any channel, but note that Shulker will
 * always use a "shulker:" prefix for internal channels.
 * <br>
 * The implementation may leverage isolated threads to both
 * subscribe and/or consume.
 */
public interface MessagingBus {
    /**
     * Subscribes to a given channel, and provides a callback to
     * consume the received messages.
     * <br>
     * One may not know whether the message was broadcasted or
     * sent specifically to a consumer.
     *
     * @param channel Channel to listen from
     * @param callback Function to execute when a message is received
     */
    void subscribe(@NotNull String channel, @NotNull Consumer<@NotNull String> callback);

    /**
     * Broadcast a message to all proxies on the provided channel.
     *
     * @param channel Channel to publish to
     * @param message Message to publish
     */
    void broadcastToAllProxies(@NotNull String channel, @NotNull String message);

    /**
     * Send a message to a specific proxy on the provided channel.
     *
     * @param proxyName Identifier of the proxy
     * @param channel Channel to publish to
     * @param message Message to publish
     */
    void sendToProxy(@NotNull String proxyName, @NotNull String channel, @NotNull String message);

    /**
     * Broadcast a message to all servers on the provided channel.
     *
     * @param channel Channel to publish to
     * @param message Message to publish
     */
    void broadcastToAllServers(@NotNull String channel, @NotNull String message);

    /**
     * Send a message to a specific server on the provided channel.
     *
     * @param serverName Identifier of the server
     * @param channel Channel to publish to
     * @param message Message to publish
     */
    void sendToServer(@NotNull String serverName, @NotNull String channel, @NotNull String message);
}
