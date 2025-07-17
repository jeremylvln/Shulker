package io.shulkermc.cluster.api.data;

import org.jetbrains.annotations.NotNull;

/**
 * Class representing the current position of a player connected
 * on the Shulker cluster.
 *
 * @param proxyName The identifier of the proxy the player is currently connected to
 * @param serverName The identifier of the server the player is currently connected to
 */
public record PlayerPosition(@NotNull String proxyName, @NotNull String serverName) {}
