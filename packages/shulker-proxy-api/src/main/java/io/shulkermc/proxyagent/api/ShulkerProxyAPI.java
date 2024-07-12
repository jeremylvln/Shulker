package io.shulkermc.proxyagent.api;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class ShulkerProxyAPI {
    public static ShulkerProxyAPI INSTANCE;

    abstract public void reconnectPlayerToCluster(@NotNull UUID playerId) throws UnsupportedOperationException;

    abstract public @NotNull Set<String> getServersByTag(@NotNull String tag);

    abstract public @NotNull Optional<PlayerPosition> getPlayerPosition(@NotNull UUID playerId);
    abstract public boolean isPlayerConnected(@NotNull UUID playerId);
    abstract public int countOnlinePlayers();

    abstract public @NotNull Optional<UUID> getPlayerIdFromName(@NotNull String playerName);
    abstract public @NotNull Optional<String> getPlayerNameFromId(@NotNull UUID playerId);

    public record PlayerPosition(@NotNull String proxyName, @NotNull String serverName) {}
}
