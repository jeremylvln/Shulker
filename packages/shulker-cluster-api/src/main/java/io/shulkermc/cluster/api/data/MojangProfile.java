package io.shulkermc.cluster.api.data;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MojangProfile(@NotNull UUID playerId, @NotNull String playerName) {}
