package io.shulkermc.proxy.api;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class ShulkerProxyAPI {
    private static ShulkerProxyAPI INSTANCE;

    ShulkerProxyAPI() {
        INSTANCE = this;
    }

    public static ShulkerProxyAPI instance() {
        return INSTANCE;
    }

    abstract public void askShutdown();

    abstract public @NotNull Set<String> getServersByTag(@NotNull String tag);
}
