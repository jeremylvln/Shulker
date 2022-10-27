package io.shulkermc.proxyagent;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.logging.Logger;

@Plugin(
    id = "shulker-proxy-agent",
    name = "ShulkerProxyAgent",
    version = "0.0.1",
    authors = {"Jérémy Levilain <jeremy@jeremylvln.fr>"}
)
public class BootstrapPlugin {
    private final ShulkerProxyAgent plugin;

    @Inject
    public BootstrapPlugin(ProxyServer server, Logger logger) {
        this.plugin = new ShulkerProxyAgent(this, server, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.plugin.onProxyInitialization(event);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.plugin.onProxyShutdown(event);
    }
}
