package io.shulkermc.proxyagent.api;

import io.shulkermc.agent.api.ShulkerAPI;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ShulkerProxyAPI implements ShulkerAPI {
    public static ShulkerProxyAPI INSTANCE;

    abstract public void shutdown();

    abstract public void reconnectPlayerToCluster(@NotNull UUID playerId);
}
