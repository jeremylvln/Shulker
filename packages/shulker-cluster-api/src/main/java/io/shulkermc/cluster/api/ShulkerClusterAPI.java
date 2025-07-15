package io.shulkermc.cluster.api;

import io.shulkermc.cluster.api.data.PlayerPosition;
import io.shulkermc.cluster.api.messaging.MessagingBus;
import io.shulkermc.sdk.ShulkerSDK;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class ShulkerClusterAPI {
    private static ShulkerClusterAPI INSTANCE;

    ShulkerClusterAPI() {
        INSTANCE = this;
    }

    public static @NotNull ShulkerClusterAPI instance() {
        return INSTANCE;
    }

    abstract public @NotNull ShulkerSDK operator();
    abstract public @NotNull MessagingBus messaging();

    abstract public void teleportPlayerOnServer(@NotNull UUID playerId, @NotNull String serverName);
    abstract public void disconnectPlayerFromCluster(@NotNull UUID playerId, @NotNull Component message);
    abstract public void reconnectPlayerToCluster(@NotNull UUID playerId);
    abstract public @NotNull Optional<@NotNull PlayerPosition> getPlayerPosition(@NotNull UUID playerId);
    abstract public boolean isPlayerConnected(@NotNull UUID playerId);
    abstract public int countOnlinePlayers();

    abstract public @NotNull Optional<@NotNull UUID> getPlayerIdFromName(@NotNull String playerName);
    abstract public @NotNull Optional<@NotNull String> getPlayerNameFromId(@NotNull UUID playerId);
}
