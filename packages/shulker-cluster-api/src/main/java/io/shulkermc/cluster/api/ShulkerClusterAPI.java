package io.shulkermc.cluster.api;

import io.shulkermc.cluster.api.data.PlayerPosition;
import io.shulkermc.cluster.api.messaging.MessagingBus;
import io.shulkermc.sdk.ShulkerSDK;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Shared API available in any environment running a Shulker agent.
 * It can be used to interact with the state of the Shulker cluster.
 * Side-specific methods may be available in dedicated sided APIs.
 */
public abstract class ShulkerClusterAPI {
    private static ShulkerClusterAPI INSTANCE;

    ShulkerClusterAPI() {
        INSTANCE = this;
    }

    /**
     * Gets the shared instance of the API. It is the responsibility
     * of the Agent plugin running alongside to initialize it.
     *
     * @return The shared instance
     */
    public static @NotNull ShulkerClusterAPI instance() {
        return INSTANCE;
    }

    /**
     * Gets (and initialize if it is the first call) an instance of the
     * gRPC SDK to interact with Shulker Operator.
     *
     * @return An instance of the gRPC SDK
     */
    abstract public @NotNull ShulkerSDK operator();

    /**
     * Gets the instance of the messaging bus. Can be used to send and/or
     * receive messages from any component in the Shulker cluster.
     *
     * @see MessagingBus
     * @return An instance of the messaging bus
     */
    abstract public @NotNull MessagingBus messaging();

    /**
     * Teleports a player connected on the Shulker cluster (on any proxy) to
     * the provided server.
     * <br>
     * This function will do nothing if the player is not connected and/or
     * the server is unknown.
     *
     * @param playerId Unique ID of the player to teleport
     * @param serverName Server identifier to send the player to
     */
    abstract public void teleportPlayerOnServer(@NotNull UUID playerId, @NotNull String serverName);

    /**
     * Kicks a player connected on the Shulker cluster with the provided message.
     * <br>
     * This function will do nothing if the player is not connected.
     *
     * @param playerId Unique ID of the player to kick
     * @param message Message to show as kick reason
     */
    abstract public void disconnectPlayerFromCluster(@NotNull UUID playerId, @NotNull Component message);

    /**
     * Disconnects then reconnect the player to the Shulker cluster. The player
     * will be reconnected to the same proxy fleet as they are currently
     * connected.
     * <br>
     * This function will do nothing if the player is not connected, or if
     * the fleet the player is currently connected on does not support the
     * reconnection feature.
     *
     * @param playerId Unique ID of the player to reconnect
     */
    abstract public void reconnectPlayerToCluster(@NotNull UUID playerId);

    /**
     * Retrieves the current position of a player connected on the Shulker
     * cluster.
     * <br>
     * This function may return an empty {Optional} if the player is not
     * currently connected.
     *
     * @see PlayerPosition
     *
     * @param playerId Unique ID of the player to retrieve the position
     * @return The current position of the player, if connected
     */
    abstract public @NotNull Optional<@NotNull PlayerPosition> getPlayerPosition(@NotNull UUID playerId);

    /**
     * Returns whether the provided player is connected to any proxy of
     * the Shulker cluster.
     *
     * @param playerId Unique ID of the player to find
     * @return Whether the player is connected
     */
    abstract public boolean isPlayerConnected(@NotNull UUID playerId);

    /**
     * Returns the current number of players connected to the Shulker cluster.
     *
     * @return The number of connected players
     */
    abstract public int countOnlinePlayers();

    /**
     * Finds the related Unique ID corresponding to the provided player
     * username.
     * <br>
     * If this information is not known and cached locally, it will be
     * fetched from Mojang API.
     * <br>
     * This function may return an empty {Optional} if the username is
     * invalid or is not used by any registered player.
     *
     * @see ShulkerClusterAPI#getPlayerNameFromId(UUID)
     *
     * @param playerName Username of the player to identify
     * @return The related Unique ID, if existing
     */
    abstract public @NotNull Optional<@NotNull UUID> getPlayerIdFromName(@NotNull String playerName);

    /**
     * Finds the related username corresponding to the provided player
     * Unique ID.
     * <br>
     * If this information is not known and cached locally, it will be
     * fetched from Mojang API.
     * <br>
     * This function may return an empty {Optional} if the Unique ID is
     * not used by any registered player.
     *
     * @see ShulkerClusterAPI#getPlayerIdFromName(String)
     *
     * @param playerId Unique ID of the player to identify
     * @return The related username, if existing
     */
    abstract public @NotNull Optional<@NotNull String> getPlayerNameFromId(@NotNull UUID playerId);
}
