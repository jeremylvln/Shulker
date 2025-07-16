package io.shulkermc.cluster.api.data;

import org.jetbrains.annotations.NotNull;

public record PlayerPosition(@NotNull String proxyName, @NotNull String serverName) {}
