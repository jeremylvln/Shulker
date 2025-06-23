package io.shulkermc.agent.api;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ShulkerAPI {
    @NotNull Set<String> getServersByTag(@NotNull String tag);

//    @NotNull Set<UUID> getOnlinePlayers();
    int countOnlinePlayers();
    @NotNull Optional<PlayerPosition> getPlayerPosition(@NotNull UUID playerId);
//    @NotNull Set<UUID> getConnectedPlayers(@NotNull String serverName);
    boolean isPlayerConnected(@NotNull UUID playerId);
    void teleportPlayerOnServer(@NotNull UUID playerId, @NotNull String serverName);

    @NotNull Optional<UUID> getPlayerIdFromName(@NotNull String playerName);
    @NotNull Optional<String> getPlayerNameFromId(@NotNull UUID playerId);
}
