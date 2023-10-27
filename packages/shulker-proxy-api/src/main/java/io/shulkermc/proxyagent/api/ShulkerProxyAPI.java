package io.shulkermc.proxyagent.api;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class ShulkerProxyAPI {
    public static ShulkerProxyAPI INSTANCE;

    abstract public Set<String> getServersByTag(@NotNull String tag);

    abstract public Optional<PlayerPosition> getPlayerPosition(@NotNull UUID playerId);
    abstract public boolean isPlayerConnected(@NotNull UUID playerId);

    public record PlayerPosition(@NotNull String proxyName, @NotNull String serverName) {}
}
