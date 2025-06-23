package io.shulkermc.agent.api;

import org.jetbrains.annotations.NotNull;

public record PlayerPosition(@NotNull String proxyName, @NotNull String serverName) {}
