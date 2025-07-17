package io.shulkermc.server.api;

import java.util.concurrent.CompletableFuture;

/**
 * Server-sided specific API available in any server running a
 * Shulker agent. It can be used to interact with server-specific
 * components managed by Shulker. Common methods are available
 * in the Cluster API directly.
 */
public abstract class ShulkerServerAPI {
    private static ShulkerServerAPI INSTANCE;

    ShulkerServerAPI() {
        INSTANCE = this;
    }

    /**
     * Gets the shared instance of the API. It is the responsibility
     * of the Agent plugin running alongside to initialize it.
     *
     * @return The shared instance
     */
    public static ShulkerServerAPI instance() {
        return INSTANCE;
    }

    /**
     * Requests to Agones to shut down this server. It may not be
     * performed cleanly.
     * <br>
     * Following this call, there are no execution guarantees.
     */
    abstract public void askShutdown();

    /**
     * Requests to Agones to mark this server as "Ready" to accept
     * players. This information may be used externally.
     *
     * @see ShulkerServerAPI#setAllocated()
     * @see ShulkerServerAPI#setReserved(long)
     *
     * @return A future resolving once Agones' acknowledgement
     */
    abstract public CompletableFuture<Void> setReady();

    /**
     * Requests to Agones to mark this server as "Allocated". This
     * information may be used externally.
     *
     * @see ShulkerServerAPI#setReady()
     * @see ShulkerServerAPI#setReserved(long)
     *
     * @return A future resolving once Agones' acknowledgement
     */
    abstract public CompletableFuture<Void> setAllocated();

    /**
     * Requests to Agones to mark this server as "Reserved" for
     * a given period of time. This information may be used
     * externally.
     *
     * @see ShulkerServerAPI#setReady()
     * @see ShulkerServerAPI#setAllocated()
     *
     * @return A future resolving once Agones' acknowledgement
     */
    abstract public CompletableFuture<Void> setReserved(long seconds);
}
