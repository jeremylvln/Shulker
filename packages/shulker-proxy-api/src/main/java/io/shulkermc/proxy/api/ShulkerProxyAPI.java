package io.shulkermc.proxy.api;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Proxy-sided specific API available in any proxy running a
 * Shulker agent. It can be used to interact with proxy-specific
 * components managed by Shulker. Common methods are available
 * in the Cluster API directly.
 */
public abstract class ShulkerProxyAPI {
    private static ShulkerProxyAPI INSTANCE;

    ShulkerProxyAPI() {
        INSTANCE = this;
    }

    /**
     * Gets the shared instance of the API. It is the responsibility
     * of the Agent plugin running alongside to initialize it.
     *
     * @return The shared instance
     */
    public static ShulkerProxyAPI instance() {
        return INSTANCE;
    }

    /**
     * Requests to Agones to shut down this proxy. It may not be
     * performed cleanly.
     * <br>
     * Following this call, there are no execution guarantees.
     */
    abstract public void askShutdown();

    /**
     * Retrieves the list of server names applicable to the provided
     * tag.
     *
     * @param tag Tag to filter
     * @return The list of applicable server names
     */
    abstract public @NotNull Set<String> getServersByTag(@NotNull String tag);
}
