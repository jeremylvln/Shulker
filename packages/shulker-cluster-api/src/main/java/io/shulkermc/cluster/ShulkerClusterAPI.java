package io.shulkermc.cluster;

import io.shulkermc.cluster.data.PlayerPosition;
import io.shulkermc.cluster.messaging.MessagingBus;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class ShulkerClusterAPI {
    public static ShulkerClusterAPI INSTANCE;

    abstract public MessagingBus getMessagingBus();

    abstract public void teleportPlayerOnServer(@NotNull UUID playerId, @NotNull String serverName);
    abstract public void disconnectPlayerFromCluster(@NotNull UUID playerId, @NotNull Component message);
    abstract public void reconnectPlayerToCluster(@NotNull UUID playerId);
    abstract public @NotNull Optional<PlayerPosition> getPlayerPosition(@NotNull UUID playerId);
    abstract public boolean isPlayerConnected(@NotNull UUID playerId);
    abstract public int countOnlinePlayers();

    abstract public @NotNull Optional<UUID> getPlayerIdFromName(@NotNull String playerName);
    abstract public @NotNull Optional<String> getPlayerNameFromId(@NotNull UUID playerId);
}
