package io.shulkermc.proxyagent.api;

import java.util.Set;

public abstract class ShulkerProxyAPI {
    public static ShulkerProxyAPI INSTANCE;

    abstract public Set<String> getServersByTag(String tag);
}
