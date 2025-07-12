package io.shulkermc.cluster.data;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record RegisteredProxy(@NotNull String proxyName, int proxyCapacity, Instant lastSeenAt) {}
