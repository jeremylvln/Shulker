package io.shulkermc.proxyagent.api;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class ShulkerProxyAPI {
    public static ShulkerProxyAPI INSTANCE;

    abstract public void askShutdown();

    abstract public @NotNull Set<String> getServersByTag(@NotNull String tag);
}
