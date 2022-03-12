package io.shulkermc.directory;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.Optional;

public class ServerConnectListener implements Listener {
    private static final BaseComponent[] MSG_NO_LIMBO_FOUND = new ComponentBuilder()
            .color(ChatColor.LIGHT_PURPLE)
            .bold(true)
            .append("-[ Shulker ]-\n")
            .color(ChatColor.RED)
            .bold(false)
            .append("No limbo server found, please check your cluster\nconfiguration.")
            .create();

    private final ShulkerProxyDirectory plugin;
    private final ProxyServer proxyServer;

    public ServerConnectListener(ShulkerProxyDirectory plugin) {
        this.plugin = plugin;
        this.proxyServer = ProxyServer.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        this.plugin.getLogger().info(event.getTarget().getName());
        if (!event.getTarget().getName().equals("lobby")) {
            return;
        }

        Optional<List<String>> limboList = this.plugin.getServersByTag("limbo");

        if (limboList.isEmpty() || limboList.get().isEmpty()) {
            this.plugin.getLogger().warning(String.format("No limbo server to send the player %s to", event.getPlayer().getName()));
            event.getPlayer().disconnect(MSG_NO_LIMBO_FOUND);
            return;
        }

        // TODO: how to dispatch?
        String server = limboList.get().get(0);
        this.plugin.getLogger().info(String.format("Sending the player %s to limbo server %s", event.getPlayer().getName(), server));
        event.setTarget(this.proxyServer.getServerInfo(server));
    }
}
