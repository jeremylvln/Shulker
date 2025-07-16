package io.shulkermc.cluster.api.data;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record RegisteredProxy(@NotNull String proxyName, int proxyCapacity, Instant lastSeenAt) {}
